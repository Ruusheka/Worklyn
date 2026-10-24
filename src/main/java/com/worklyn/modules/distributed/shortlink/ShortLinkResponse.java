package com.worklyn.modules.distributed.shortlink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkResponse {
    private String id;
    private String shortCode;
    private String shortUrl;
    private String destinationUrl;
    private long clickCount;
    private Instant expiresAt;
    private Instant createdAt;
}
