package com.worklyn.common.security;

import com.worklyn.modules.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserRepository users;
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository users){this.tokenProvider=tokenProvider;this.users=users;}
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication()==null) {
            String token=header.substring(7).trim();
            if (!token.isEmpty() && tokenProvider.validateToken(token)) {
                try {
                    String userId=tokenProvider.getUsernameFromToken(token);
                    users.findById(userId).filter(u -> u.isEnabled()).ifPresent(user -> {
                        UserPrincipal principal=UserPrincipal.from(user);
                        var authentication=new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
                } catch (RuntimeException ignored) {
                    SecurityContextHolder.clearContext();
                }
            }
        }
        chain.doFilter(request,response);
    }
}
