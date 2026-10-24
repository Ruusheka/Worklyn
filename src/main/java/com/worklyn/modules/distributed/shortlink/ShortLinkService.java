package com.worklyn.modules.distributed.shortlink;

import com.worklyn.common.exception.BadRequestException;
import com.worklyn.common.exception.ResourceNotFoundException;
import com.worklyn.modules.distributed.cache.DistributedCacheService;
import com.worklyn.modules.distributed.idgen.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Service for creating and resolving short links.
 *
 * <p><strong>SSRF Protection:</strong> Before creating a link, the destination URL is validated
 * to ensure it does not point to private, loopback, link-local, or other internal addresses.
 * DNS resolution is checked. No actual HTTP fetch of the destination is performed during creation.</p>
 *
 * <p><strong>Code generation:</strong> A 7-character Base62 code is derived from the Snowflake ID.
 * On collision, up to 3 retries are attempted with different offsets.</p>
 *
 * <p><strong>Caching:</strong> Resolved destination URLs are cached in Redis under key
 * {@code sl:{code}} with a 24-hour TTL. The cache is invalidated when a link expires.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_RETRIES = 3;
    private static final String CACHE_PREFIX = "sl:";

    private final ShortLinkRepository repository;
    private final DistributedCacheService cacheService;
    private final SnowflakeIdGenerator idGenerator;
    private final MongoTemplate mongoTemplate;

    @Value("${app.shortlink.base-url:http://localhost:8080/s/}")
    private String baseUrl;

    @Value("${app.shortlink.default-ttl-days:30}")
    private int defaultTtlDays;

    /**
     * Create a short link for the given destination URL.
     *
     * @param destinationUrl the full URL to shorten (must be HTTP/HTTPS, non-internal)
     * @param ttlDays        optional custom TTL in days; null uses the configured default
     * @param creatorId      the authenticated user's ID
     * @return the persisted short link response
     * @throws BadRequestException if the URL is invalid, private, or SSRF-risky
     */
    public ShortLinkResponse createShortLink(String destinationUrl, Long ttlDays, String creatorId) {
        if (destinationUrl == null || destinationUrl.isBlank()) {
            throw new BadRequestException("INVALID_URL", "Destination URL must not be blank");
        }
        if (ttlDays != null && ttlDays <= 0) {
            throw new BadRequestException("INVALID_TTL", "TTL must be a positive number of days");
        }

        validateDestinationUrl(destinationUrl);

        long effectiveTtlDays = (ttlDays != null) ? ttlDays : defaultTtlDays;
        Instant expiresAt = Instant.now().plus(Duration.ofDays(effectiveTtlDays));

        String code = null;
        long snowflakeBase = idGenerator.nextId();
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String candidate = toBase62(snowflakeBase + attempt);
            if (!repository.existsByShortCode(candidate)) {
                code = candidate;
                break;
            }
            log.debug("Short code collision on attempt {}: {}", attempt, candidate);
        }
        if (code == null) {
            throw new BadRequestException("CODE_COLLISION", "Failed to generate a unique short code after " + MAX_RETRIES + " attempts");
        }

        ShortLink link = ShortLink.builder()
                .shortCode(code)
                .destinationUrl(destinationUrl)
                .creatorId(creatorId)
                .expiresAt(expiresAt)
                .clickCount(0L)
                .createdAt(Instant.now())
                .build();

        ShortLink saved = repository.save(link);

        // Cache the mapping
        cacheService.put(CACHE_PREFIX + code, destinationUrl, Duration.ofHours(24));

        return toResponse(saved);
    }

    /**
     * Resolve a short code to its destination URL.
     * Checks Redis cache first; falls back to MongoDB on miss.
     * Increments click count asynchronously.
     *
     * @param code the 7-character short code
     * @return the destination URL
     * @throws ResourceNotFoundException if the code is unknown or expired
     */
    public String resolveShortLink(String code) {
        if (code == null || code.isBlank()) {
            throw new ResourceNotFoundException("SHORT_LINK_NOT_FOUND", "Short link not found: " + code);
        }

        // Try Redis cache first
        Optional<String> cached = cacheService.get(CACHE_PREFIX + code, String.class);
        if (cached.isPresent()) {
            incrementClickCountAsync(code);
            return cached.get();
        }

        // Cache miss: query MongoDB
        ShortLink link = repository.findByShortCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("SHORT_LINK_NOT_FOUND",
                        "Short link not found: " + code));

        if (link.getExpiresAt() != null && Instant.now().isAfter(link.getExpiresAt())) {
            cacheService.evict(CACHE_PREFIX + code);
            throw new ResourceNotFoundException("SHORT_LINK_EXPIRED", "Short link has expired: " + code);
        }

        // Re-cache
        Duration cacheTtl = link.getExpiresAt() != null
                ? Duration.between(Instant.now(), link.getExpiresAt()).abs()
                : Duration.ofHours(24);
        cacheService.put(CACHE_PREFIX + code, link.getDestinationUrl(), cacheTtl.isZero() ? Duration.ofSeconds(1) : cacheTtl);

        incrementClickCountAsync(code);
        return link.getDestinationUrl();
    }

    @Async
    void incrementClickCountAsync(String code) {
        try {
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("shortCode").is(code)),
                    new Update().inc("clickCount", 1),
                    ShortLink.class);
        } catch (Exception e) {
            log.warn("Failed to increment click count for code [{}]: {}", code, e.getMessage());
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Validates that the destination URL is safe to store (no SSRF risk).
     * Does NOT make an HTTP request to the destination.
     */
    void validateDestinationUrl(String rawUrl) {
        URI uri;
        try {
            uri = new URI(rawUrl).normalize();
        } catch (URISyntaxException e) {
            throw new BadRequestException("INVALID_URL", "Malformed URL: " + e.getMessage());
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new BadRequestException("INVALID_URL", "Only HTTP and HTTPS URLs are allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BadRequestException("INVALID_URL", "URL must have a valid hostname");
        }

        // Resolve DNS and check all resolved addresses
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (isPrivateOrInternalAddress(addr)) {
                    throw new BadRequestException("SSRF_BLOCKED",
                            "Destination URL resolves to a private or internal address, which is not permitted");
                }
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            // DNS resolution failure: treat as invalid
            throw new BadRequestException("INVALID_URL", "Could not resolve hostname: " + host);
        }
    }

    /**
     * Returns true if the address is loopback, private, link-local, or otherwise internal.
     */
    boolean isPrivateOrInternalAddress(InetAddress addr) {
        return addr.isLoopbackAddress()
                || addr.isSiteLocalAddress()
                || addr.isLinkLocalAddress()
                || addr.isMulticastAddress()
                || addr.isAnyLocalAddress()
                || isUnspecified(addr);
    }

    private boolean isUnspecified(InetAddress addr) {
        byte[] bytes = addr.getAddress();
        for (byte b : bytes) {
            if (b != 0) return false;
        }
        return true;
    }

    /** Converts a long to a 7-character Base62 string. */
    String toBase62(long value) {
        // Use absolute value to avoid issues with sign bit
        long v = Math.abs(value);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62_CHARS.charAt((int) (v % 62)));
            v /= 62;
        }
        return sb.reverse().toString();
    }

    private ShortLinkResponse toResponse(ShortLink link) {
        return ShortLinkResponse.builder()
                .id(link.getId())
                .shortCode(link.getShortCode())
                .shortUrl(baseUrl + link.getShortCode())
                .destinationUrl(link.getDestinationUrl())
                .clickCount(link.getClickCount())
                .expiresAt(link.getExpiresAt())
                .createdAt(link.getCreatedAt())
                .build();
    }
}
