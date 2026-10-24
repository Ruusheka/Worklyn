package com.worklyn.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "worklyn-test-secret-must-have-at-least-32-bytes");
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 3600000L);
        provider.initialize();
    }

    @Test
    void rejectsShortSigningSecretAtInitialization() {
        JwtTokenProvider invalid = new JwtTokenProvider();
        ReflectionTestUtils.setField(invalid, "jwtSecret", "too-short");
        ReflectionTestUtils.setField(invalid, "jwtExpirationMs", 86400000L);
        assertThrows(IllegalStateException.class, invalid::initialize);
    }

    @Test
    void rejectsNonPositiveExpiration() {
        JwtTokenProvider invalid = new JwtTokenProvider();
        ReflectionTestUtils.setField(invalid, "jwtSecret",
                "worklyn-test-secret-must-have-at-least-32-bytes");
        ReflectionTestUtils.setField(invalid, "jwtExpirationMs", 0L);
        assertThrows(IllegalStateException.class, invalid::initialize);
    }

    @Test
    void rejectsMalformedToken() {
        assertFalse(provider.validateToken("not-a-jwt"));
    }

    @Test
    void rejectsTamperedToken() {
        UserPrincipal principal = new UserPrincipal(
                "user-1", "employee@example.com", "hash", "emp-1",
                Set.of(com.worklyn.modules.auth.model.Role.ROLE_EMPLOYEE), true);
        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        String token = provider.generateToken(auth);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(provider.validateToken(tampered));
    }

    @Test
    void generatesAndExtractsTokenClaims() {
        UserPrincipal principal = new UserPrincipal(
                "user-1", "employee@example.com", "hash", "emp-1",
                Set.of(com.worklyn.modules.auth.model.Role.ROLE_EMPLOYEE), true);
        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        String token = provider.generateToken(auth);

        assertTrue(provider.validateToken(token));
        assertEquals("user-1", provider.getUsernameFromToken(token));
        assertEquals("employee@example.com", provider.getEmailFromToken(token));
        assertEquals("emp-1", provider.getEmployeeIdFromToken(token));
        assertTrue(provider.getRolesFromToken(token).contains("ROLE_EMPLOYEE"));
    }
}