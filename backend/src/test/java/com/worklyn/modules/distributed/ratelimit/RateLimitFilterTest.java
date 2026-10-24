package com.worklyn.modules.distributed.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private TokenBucketService tokenBucketService;
    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;
    private RateLimitProperties properties;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        rateLimitFilter = new RateLimitFilter(tokenBucketService, properties, objectMapper);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Allowed unauthenticated request proceeds through filter chain with X-RateLimit-Remaining header")
    void testAllowedPublicRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public-endpoint");
        request.setRemoteAddr("203.0.113.195");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBucketService.tryConsume(eq("rl:ip:203.0.113.195"), eq(20L), eq(2.0), eq(1L)))
                .thenReturn(RateLimitResult.allowed(19.0));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals("19", response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    @DisplayName("Exhausted request returns HTTP 429 Too Many Requests with retry-after header and ApiErrorResponse")
    void testExhaustedReturns429() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/public-endpoint");
        request.setRemoteAddr("203.0.113.195");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBucketService.tryConsume(eq("rl:ip:203.0.113.195"), eq(20L), eq(2.0), eq(1L)))
                .thenReturn(RateLimitResult.rejected(0.0, 5L));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(429, response.getStatus());
        assertEquals("5", response.getHeader("X-RateLimit-Retry-After-Seconds"));
        assertEquals("0", response.getHeader("X-RateLimit-Remaining"));
        assertTrue(response.getContentAsString().contains("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("Authenticated user gets user-specific rate limit bucket")
    void testAuthenticatedUserPolicy() throws ServletException, IOException {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("user-42", "pass");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/employees/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBucketService.tryConsume(eq("rl:user:user-42"), eq(100L), eq(10.0), eq(1L)))
                .thenReturn(RateLimitResult.allowed(99.0));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Expensive endpoint uses expensive policy tier")
    void testExpensiveEndpointPolicy() throws ServletException, IOException {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("admin-99", "pass");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/payroll/process");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenBucketService.tryConsume(eq("rl:expensive:admin-99"), eq(5L), eq(0.2), eq(1L)))
                .thenReturn(RateLimitResult.allowed(4.0));

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
