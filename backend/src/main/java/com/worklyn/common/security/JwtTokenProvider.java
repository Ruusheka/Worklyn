package com.worklyn.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret:}") private String jwtSecret;
    @Value("${app.jwt.expiration-ms:86400000}") private long jwtExpirationMs;
    private SecretKey key;
    @PostConstruct void initialize() {
        byte[] bytes = jwtSecret == null ? new byte[0] : jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalStateException("APP_JWT_SECRET must contain at least 32 UTF-8 bytes.");
        if (jwtExpirationMs <= 0) throw new IllegalStateException("APP_JWT_EXPIRATION_MS must be positive.");
        key = Keys.hmacShaKeyFor(bytes);
    }
    public String generateToken(Authentication auth) {
        UserPrincipal p = (UserPrincipal) auth.getPrincipal();
        Date now = new Date();
        List<String> roles = p.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        var builder = Jwts.builder().subject(p.getId()).claim("email", p.getEmail()).claim("roles", roles).issuedAt(now)
                .expiration(new Date(now.getTime()+jwtExpirationMs));
        if (p.getEmployeeId()!=null) builder.claim("empId", p.getEmployeeId());
        return builder.signWith(key).compact();
    }
    public boolean validateToken(String token) {
        try { parse(token); return true; } catch (JwtException | IllegalArgumentException ex) { return false; }
    }
    public String getUsernameFromToken(String token) { return parse(token).getSubject(); }
    public List<String> getRolesFromToken(String token) {
        Object roles=parse(token).get("roles");
        if (!(roles instanceof List<?> list)) return List.of();
        return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
    }
    public String getEmailFromToken(String token) { return parse(token).get("email", String.class); }
    public String getEmployeeIdFromToken(String token) { return parse(token).get("empId", String.class); }
    public long getExpirationMs(){return jwtExpirationMs;}
    private Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
