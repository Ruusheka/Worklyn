package com.worklyn.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<ValidationError> validationErrors
) {
    public record ValidationError(String field, Object rejectedValue, String message) {
        public static ValidationErrorBuilder builder() {
            return new ValidationErrorBuilder();
        }

        public static class ValidationErrorBuilder {
            private String field;
            private Object rejectedValue;
            private String message;

            public ValidationErrorBuilder field(String field) {
                this.field = field;
                return this;
            }

            public ValidationErrorBuilder rejectedValue(Object rejectedValue) {
                this.rejectedValue = rejectedValue;
                return this;
            }

            public ValidationErrorBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ValidationError build() {
                return new ValidationError(field, rejectedValue, message);
            }
        }
    }

    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }

    public static class ApiErrorResponseBuilder {
        private Instant timestamp = Instant.now();
        private int status;
        private String error;
        private String code;
        private String message;
        private String path;
        private List<ValidationError> validationErrors;

        public ApiErrorResponseBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ApiErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        public ApiErrorResponseBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ApiErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ApiErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ApiErrorResponseBuilder validationErrors(List<ValidationError> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(timestamp, status, error, code, message, path, validationErrors);
        }
    }
}
