package com.worklyn.modules.distributed.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitResult {

    private boolean allowed;
    private double remainingTokens;
    private long retryAfterSeconds;

    public static RateLimitResult allowed(double remainingTokens) {
        return RateLimitResult.builder()
                .allowed(true)
                .remainingTokens(remainingTokens)
                .retryAfterSeconds(0)
                .build();
    }

    public static RateLimitResult rejected(double remainingTokens, long retryAfterSeconds) {
        return RateLimitResult.builder()
                .allowed(false)
                .remainingTokens(remainingTokens)
                .retryAfterSeconds(retryAfterSeconds)
                .build();
    }
}
