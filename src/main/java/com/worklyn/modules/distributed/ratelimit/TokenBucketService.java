package com.worklyn.modules.distributed.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBucketService {

    private final StringRedisTemplate redisTemplate;
    private Clock clock = Clock.systemUTC();

    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refillRate = tonumber(ARGV[2])
            local requested = tonumber(ARGV[3])
            local now = tonumber(ARGV[4])

            local data = redis.call('HMGET', key, 'tokens', 'lastRefreshed')
            local tokens = tonumber(data[1])
            local lastRefreshed = tonumber(data[2])

            if not tokens or not lastRefreshed then
                tokens = capacity
                lastRefreshed = now
            else
                local delta = math.max(0, now - lastRefreshed) / 1000.0
                tokens = math.min(capacity, tokens + (delta * refillRate))
                lastRefreshed = now
            end

            if tokens >= requested then
                tokens = tokens - requested
                redis.call('HMSET', key, 'tokens', tostring(tokens), 'lastRefreshed', tostring(lastRefreshed))
                local expireSeconds = math.max(60, math.ceil((capacity / refillRate) * 2))
                redis.call('EXPIRE', key, expireSeconds)
                return {1, tostring(tokens), 0}
            else
                local waitSeconds = (requested - tokens) / refillRate
                return {0, tostring(tokens), math.ceil(waitSeconds)}
            end
            """;

    private final DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(LUA_SCRIPT, List.class);

    // Allow clock override for deterministic testing
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Atomically attempts to consume tokens from the bucket key.
     * Fails open if Redis is unreachable.
     */
    public RateLimitResult tryConsume(String bucketKey, long capacity, double refillRate, long requestedTokens) {
        if (bucketKey == null || bucketKey.isBlank()) {
            throw new IllegalArgumentException("Bucket key must not be null or blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        if (refillRate <= 0.0) {
            throw new IllegalArgumentException("Refill rate must be positive");
        }
        if (requestedTokens <= 0) {
            throw new IllegalArgumentException("Requested tokens must be positive");
        }

        long now = clock.millis();

        try {
            List<?> result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(bucketKey),
                    String.valueOf(capacity),
                    String.valueOf(refillRate),
                    String.valueOf(requestedTokens),
                    String.valueOf(now)
            );

            if (result == null || result.size() < 3) {
                log.warn("Rate limiter script returned unexpected result: {}. Failing open.", result);
                return RateLimitResult.allowed(capacity);
            }

            long allowed = Long.parseLong(result.get(0).toString());
            double remainingTokens = Double.parseDouble(result.get(1).toString());
            long retryAfterSeconds = Long.parseLong(result.get(2).toString());

            if (allowed == 1L) {
                return RateLimitResult.allowed(remainingTokens);
            } else {
                return RateLimitResult.rejected(remainingTokens, retryAfterSeconds);
            }
        } catch (Exception e) {
            log.warn("Redis connectivity failure during rate limit check for bucket [{}]: {}. Applying fail-open policy.",
                    bucketKey, e.getMessage());
            return RateLimitResult.allowed(capacity);
        }
    }
}
