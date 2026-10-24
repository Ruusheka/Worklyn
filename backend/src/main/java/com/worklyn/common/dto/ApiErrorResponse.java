package com.worklyn.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiErrorResponse(Instant timestamp, int status, String error, String code,
                               String message, String path, List<ValidationError> validationErrors) {
    public record ValidationError(String field, Object rejectedValue, String message) { }
}
