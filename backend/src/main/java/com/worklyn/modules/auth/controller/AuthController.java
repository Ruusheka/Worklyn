package com.worklyn.modules.auth.controller;

import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.auth.dto.*;
import com.worklyn.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service){this.service=service;}
    @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest request){return service.login(request);}
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    public UserResponse register(@Valid @RequestBody RegisterRequest request, Authentication authentication) {
        String role=authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))?"ROLE_ADMIN":"";
        return service.register(request,role);
    }
    @GetMapping("/me") @PreAuthorize("isAuthenticated()")
    public UserResponse me(@org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal principal){return service.getCurrentUser(principal);}
}
