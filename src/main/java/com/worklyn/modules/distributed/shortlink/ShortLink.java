package com.worklyn.modules.distributed.shortlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a persisted short link.
 * Collection: {@code short_links}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "short_links")
public class ShortLink {

    @Id
    private String id;

    /** 7-character Base62 short code, unique. */
    @Indexed(unique = true)
    private String shortCode;

    /** The original destination URL. */
    private String destinationUrl;

    /** The user ID of the creator (null for unauthenticated if allowed). */
    private String creatorId;

    /** Number of times this link has been resolved. */
    @Builder.Default
    private long clickCount = 0L;

    /** When this link expires (null = no expiry). */
    private Instant expiresAt;

    @CreatedDate
    private Instant createdAt;
}
