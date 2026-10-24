package com.worklyn.common.exception;

import com.worklyn.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiErrorResponse.ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toValidationError).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Input validation failed for " + errors.size() + " fields.", req, errors);
    }
    private ApiErrorResponse.ValidationError toValidationError(FieldError e) {
        Object rejected = e.getField().toLowerCase().contains("password") ? "[REDACTED]" : e.getRejectedValue();
        return new ApiErrorResponse.ValidationError(e.getField(), rejected, e.getDefaultMessage());
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return response(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), req, null);
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, HttpServletRequest req) {
        return response(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), req, null);
    }
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateKeyException ex, HttpServletRequest req) {
        return response(HttpStatus.CONFLICT, "DUPLICATE_KEY", "A record with the same unique value already exists.", req, null);
    }
    @ExceptionHandler({BadCredentialsException.class, org.springframework.security.core.userdetails.UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(Exception ex, HttpServletRequest req) {
        return response(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Email or password is incorrect.", req, null);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission to perform this action.", req, null);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), req, null);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed.", req, null);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error for path {}", req.getRequestURI(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.", req, null);
    }
    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message,
            HttpServletRequest req, List<ApiErrorResponse.ValidationError> errors) {
        ApiErrorResponse body = new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), code,
                message, req.getRequestURI(), errors);
        return ResponseEntity.status(status).body(body);
    }
}
