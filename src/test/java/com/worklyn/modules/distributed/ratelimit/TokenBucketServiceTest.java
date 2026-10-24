package com.worklyn.modules.distributed.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBucketServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private TokenBucketService tokenBucketService;

    @BeforeEach
    void setUp() {
        tokenBucketService = new TokenBucketService(redisTemplate);
    }

    @Test
    @DisplayName("Allowed token consumption returns allowed=true and remaining tokens")
    void testAllowedConsumption() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), eq(Collections.singletonList("rl:user:1")), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(1L, "99.0", 0L));

        RateLimitResult result = tokenBucketService.tryConsume("rl:user:1", 100, 10.0, 1);

        assertTrue(result.isAllowed());
        assertEquals(99.0, result.getRemainingTokens());
        assertEquals(0, result.getRetryAfterSeconds());
    }

    @Test
    @DisplayName("Exhausted bucket returns allowed=false with calculated retry-after seconds")
    void testExhaustedBucket() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), eq(Collections.singletonList("rl:user:2")), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(0L, "0.0", 4L));

        RateLimitResult result = tokenBucketService.tryConsume("rl:user:2", 100, 10.0, 1);

        assertFalse(result.isAllowed());
        assertEquals(0.0, result.getRemainingTokens());
        assertEquals(4L, result.getRetryAfterSeconds());
    }

    @Test
    @DisplayName("Fail-open policy when Redis is unavailable")
    void testFailOpenOnRedisFailure() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), eq(Collections.singletonList("rl:user:down")), any(), any(), any(), any()))
                .thenThrow(new RedisConnectionFailureException("Redis connection refused"));

        RateLimitResult result = tokenBucketService.tryConsume("rl:user:down", 100, 10.0, 1);

        assertTrue(result.isAllowed(), "Fail-open policy must allow request when Redis is unavailable");
        assertEquals(100.0, result.getRemainingTokens());
    }

    @Test
    @DisplayName("Validation prevents non-positive capacity, rate, or tokens")
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> tokenBucketService.tryConsume("", 10, 1.0, 1));
        assertThrows(IllegalArgumentException.class, () -> tokenBucketService.tryConsume("k", 0, 1.0, 1));
        assertThrows(IllegalArgumentException.class, () -> tokenBucketService.tryConsume("k", 10, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> tokenBucketService.tryConsume("k", 10, 1.0, 0));
    }
}
