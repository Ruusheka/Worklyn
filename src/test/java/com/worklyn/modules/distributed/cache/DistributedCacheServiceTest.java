package com.worklyn.modules.distributed.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private DistributedCacheService cacheService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cacheService = new DistributedCacheService(redisTemplate, objectMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Cache hit returns deserialized object")
    void testCacheHit() {
        when(valueOperations.get("emp:123")).thenReturn("{\"id\":\"123\",\"name\":\"David\"}");

        Optional<TestUser> result = cacheService.get("emp:123", TestUser.class);
        assertTrue(result.isPresent());
        assertEquals("123", result.get().id);
        assertEquals("David", result.get().name);
    }

    @Test
    @DisplayName("Cache miss returns Optional.empty()")
    void testCacheMiss() {
        when(valueOperations.get("emp:404")).thenReturn(null);

        Optional<TestUser> result = cacheService.get("emp:404", TestUser.class);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Malformed data treated as cache miss")
    void testMalformedData() {
        when(valueOperations.get("emp:corrupt")).thenReturn("{invalid-json-data");

        Optional<TestUser> result = cacheService.get("emp:corrupt", TestUser.class);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Redis connection failure fails open and returns Optional.empty()")
    void testRedisDownFailOpen() {
        when(valueOperations.get("emp:err")).thenThrow(new RedisConnectionFailureException("Connection refused"));

        Optional<TestUser> result = cacheService.get("emp:err", TestUser.class);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Put stores serialized JSON with explicit TTL")
    void testPutWithTtl() {
        TestUser user = new TestUser("U1", "Alice");
        cacheService.put("user:U1", user, Duration.ofMinutes(10));

        verify(valueOperations, times(1)).set(eq("user:U1"), anyString(), eq(Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("Invalid key or TTL throws IllegalArgumentException")
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> cacheService.get("", TestUser.class));
        assertThrows(IllegalArgumentException.class, () -> cacheService.put("k", new TestUser("1", "A"), Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> cacheService.put("k", new TestUser("1", "A"), Duration.ofSeconds(-5)));
    }

    public static class TestUser {
        public String id;
        public String name;
        public TestUser() {}
        public TestUser(String id, String name) { this.id = id; this.name = name; }
    }
}
