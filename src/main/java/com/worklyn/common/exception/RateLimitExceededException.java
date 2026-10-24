package com.worklyn.common.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {
    private final String code;
    private final long retryAfterSeconds;
    private final double remainingTokens;

    public RateLimitExceededException(String message, long retryAfterSeconds, double remainingTokens) {
        super(message);
        this.code = "RATE_LIMIT_EXCEEDED";
        this.retryAfterSeconds = retryAfterSeconds;
        this.remainingTokens = remainingTokens;
    }
}
