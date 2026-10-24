package com.worklyn.modules.distributed.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Retrieve a cached object by key, deserializing from JSON.
     * If Redis is down or key is malformed/absent, returns Optional.empty() to allow DB fallback.
     */
    public <T> Optional<T> get(String key, Class<T> clazz) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cache key must not be null or blank");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Target class must not be null");
        }

        try {
            String rawJson = redisTemplate.opsForValue().get(key);
            if (rawJson == null || rawJson.isBlank()) {
                return Optional.empty();
            }
            T value = objectMapper.readValue(rawJson, clazz);
            return Optional.ofNullable(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached value for key [{}]: {}. Treating as cache miss.", key, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Redis connectivity error during get for key [{}]: {}. Falling back to source.", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Cache an object with an explicit TTL.
     */
    public void put(String key, Object value, Duration ttl) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cache key must not be null or blank");
        }
        if (value == null) {
            throw new IllegalArgumentException("Cache value must not be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Cache TTL must be non-null and positive");
        }

        try {
            String rawJson = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, rawJson, ttl);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cache value for key [{}]: {}", key, e.getMessage());
        } catch (Exception e) {
            log.warn("Redis connectivity error during put for key [{}]: {}", key, e.getMessage());
        }
    }

    /**
     * Evict a single cache key.
     */
    public void evict(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cache key must not be null or blank");
        }

        try {
            redisTemplate.unlink(key);
        } catch (Exception e) {
            log.warn("Redis connectivity error during evict for key [{}]: {}", key, e.getMessage());
        }
    }

    /**
     * Invalidate all keys matching a prefix using non-blocking SCAN in batches of 100.
     * Never uses blocking KEYS *.
     */
    public void evictByPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Cache prefix must not be null or blank");
        }

        String pattern = prefix.endsWith(":") ? prefix + "*" : prefix + ":*";
        try {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
            List<String> keysToDelete = new ArrayList<>();

            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keysToDelete.add(cursor.next());
                    if (keysToDelete.size() >= 100) {
                        redisTemplate.unlink(keysToDelete);
                        keysToDelete.clear();
                    }
                }
            }

            if (!keysToDelete.isEmpty()) {
                redisTemplate.unlink(keysToDelete);
            }
            log.debug("Successfully scanned and evicted keys with prefix pattern [{}]", pattern);
        } catch (Exception e) {
            log.warn("Redis connectivity error during evictByPrefix for pattern [{}]: {}", pattern, e.getMessage());
        }
    }
}
