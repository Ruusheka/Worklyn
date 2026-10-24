package com.worklyn.modules.distributed.shortlink;

import com.worklyn.common.exception.BadRequestException;
import com.worklyn.modules.distributed.cache.DistributedCacheService;
import com.worklyn.modules.distributed.idgen.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortLinkServiceTest {

    @Mock
    private ShortLinkRepository repository;
    @Mock
    private DistributedCacheService cacheService;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private MongoTemplate mongoTemplate;

    private ShortLinkService shortLinkService;

    @BeforeEach
    void setUp() {
        shortLinkService = new ShortLinkService(repository, cacheService, idGenerator, mongoTemplate);
        ReflectionTestUtils.setField(shortLinkService, "baseUrl", "http://worklyn.io/s/");
        ReflectionTestUtils.setField(shortLinkService, "defaultTtlDays", 30);
    }

    @Test
    @DisplayName("SSRF protection: rejects localhost, loopback, and internal IPs")
    void testSsrfProtection() throws UnknownHostException {
        assertTrue(shortLinkService.isPrivateOrInternalAddress(InetAddress.getByName("127.0.0.1")));
        assertTrue(shortLinkService.isPrivateOrInternalAddress(InetAddress.getByName("10.0.0.1")));
        assertTrue(shortLinkService.isPrivateOrInternalAddress(InetAddress.getByName("172.16.0.1")));
        assertTrue(shortLinkService.isPrivateOrInternalAddress(InetAddress.getByName("192.168.1.1")));
        assertTrue(shortLinkService.isPrivateOrInternalAddress(InetAddress.getByName("169.254.169.254")));
        assertTrue(shortLinkService.isPrivateOrInternalAddress(InetAddress.getByName("::1")));

        assertThrows(BadRequestException.class, () -> shortLinkService.createShortLink("http://127.0.0.1:8080/admin", null, "USER1"));
        assertThrows(BadRequestException.class, () -> shortLinkService.createShortLink("http://localhost:8080/actuator", null, "USER1"));
    }

    @Test
    @DisplayName("Create short link generates Base62 7-char code and caches in Redis")
    void testCreateShortLink() {
        when(idGenerator.nextId()).thenReturn(123456789L);
        when(repository.existsByShortCode(anyString())).thenReturn(false);
        when(repository.save(any(ShortLink.class))).thenAnswer(invocation -> {
            ShortLink sl = invocation.getArgument(0);
            sl.setId("mongo-id-1");
            return sl;
        });

        ShortLinkResponse resp = shortLinkService.createShortLink("https://worklyn.io/docs/payroll", 15L, "HR-ADMIN");

        assertNotNull(resp);
        assertEquals(7, resp.getShortCode().length());
        assertEquals("https://worklyn.io/docs/payroll", resp.getDestinationUrl());
        verify(cacheService, times(1)).put(eq("sl:" + resp.getShortCode()), eq("https://worklyn.io/docs/payroll"), any());
    }

    @Test
    @DisplayName("Resolve short link hits Redis cache first")
    void testResolveCacheHit() {
        when(cacheService.get("sl:abc1234", String.class)).thenReturn(Optional.of("https://target.com"));

        String dest = shortLinkService.resolveShortLink("abc1234");
        assertEquals("https://target.com", dest);
        verify(repository, never()).findByShortCode(anyString());
    }

    @Test
    @DisplayName("Resolve short link falls back to Mongo on cache miss")
    void testResolveCacheMiss() {
        when(cacheService.get("sl:xyz9876", String.class)).thenReturn(Optional.empty());
        ShortLink link = ShortLink.builder()
                .shortCode("xyz9876")
                .destinationUrl("https://target-from-mongo.com")
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(repository.findByShortCode("xyz9876")).thenReturn(Optional.of(link));

        String dest = shortLinkService.resolveShortLink("xyz9876");
        assertEquals("https://target-from-mongo.com", dest);
        verify(cacheService, times(1)).put(eq("sl:xyz9876"), eq("https://target-from-mongo.com"), any());
    }
}
