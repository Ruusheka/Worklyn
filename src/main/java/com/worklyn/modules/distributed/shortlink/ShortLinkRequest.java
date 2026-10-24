package com.worklyn.modules.distributed.shortlink;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkRequest {
    @NotBlank(message = "Destination URL must not be blank")
    private String destinationUrl;

    /** Optional TTL in days. If omitted, uses the configured default. */
    private Long ttlDays;
}
