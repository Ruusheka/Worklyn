package com.worklyn.common.exception;

import lombok.Getter;

@Getter
public class InvariantViolationException extends RuntimeException {
    private final String code;

    public InvariantViolationException(String message) {
        super(message);
        this.code = "INVARIANT_VIOLATION";
    }

    public InvariantViolationException(String code, String message) {
        super(message);
        this.code = code;
    }
}
