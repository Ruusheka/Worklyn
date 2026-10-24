package com.worklyn.modules.distributed.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worklyn.common.dto.ApiErrorResponse;
import com.worklyn.common.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final TokenBucketService tokenBucketService;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);

        String bucketKey;
        long capacity;
        double refillRate;

        boolean isExpensive = isExpensiveEndpoint(path, request.getMethod());

        if (isAuthenticated) {
            String userId = extractUserId(auth);
            if (isExpensive) {
                bucketKey = "rl:expensive:" + userId;
                capacity = properties.getExpensive().getCapacity();
                refillRate = properties.getExpensive().getRefillRate();
            } else {
                bucketKey = "rl:user:" + userId;
                capacity = properties.getAuthenticated().getCapacity();
                refillRate = properties.getAuthenticated().getRefillRate();
            }
        } else {
            String clientIp = resolveClientIp(request);
            if (isExpensive) {
                bucketKey = "rl:expensive:ip:" + clientIp;
                capacity = properties.getExpensive().getCapacity();
                refillRate = properties.getExpensive().getRefillRate();
            } else {
                bucketKey = "rl:ip:" + clientIp;
                capacity = properties.getPublicTier().getCapacity();
                refillRate = properties.getPublicTier().getRefillRate();
            }
        }

        RateLimitResult result = tokenBucketService.tryConsume(bucketKey, capacity, refillRate, 1);

        response.setHeader("X-RateLimit-Remaining", String.valueOf((long) Math.max(0, result.getRemainingTokens())));

        if (!result.isAllowed()) {
            response.setHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(result.getRetryAfterSeconds()));
            send429Error(request, response, result.getRetryAfterSeconds());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExpensiveEndpoint(String path, String method) {
        if ("POST".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/v1/payroll/process")) {
                return true;
            }
            if (path.startsWith("/api/v1/rewind/workflows/") && path.endsWith("/recover")) {
                return true;
            }
        }
        return false;
    }

    private String extractUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal up && up.getId() != null) {
            return up.getId();
        }
        return auth.getName();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] parts = xForwardedFor.split(",");
            for (String part : parts) {
                String ip = part.trim();
                if (isValidIp(ip) && !isPrivateOrLocalIp(ip)) {
                    return ip;
                }
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "127.0.0.1";
    }

    private boolean isValidIp(String ip) {
        return IPV4_PATTERN.matcher(ip).matches() || ip.contains(":");
    }

    private boolean isPrivateOrLocalIp(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127.") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }

    private void send429Error(HttpServletRequest request, HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                .code("RATE_LIMIT_EXCEEDED")
                .message("Token bucket exhausted. Please wait " + retryAfterSeconds + " seconds before retrying.")
                .path(request.getRequestURI())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
        response.getWriter().flush();
    }
}
