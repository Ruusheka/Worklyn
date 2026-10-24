package com.worklyn.modules.auth.service;

import com.worklyn.common.exception.ConflictException;
import com.worklyn.common.exception.ResourceNotFoundException;
import com.worklyn.common.security.JwtTokenProvider;
import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.auth.dto.*;
import com.worklyn.modules.auth.model.Role;
import com.worklyn.modules.auth.model.User;
import com.worklyn.modules.auth.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtTokenProvider tokens; private final AuthenticationManager authenticationManager;
    public AuthService(UserRepository users, PasswordEncoder encoder, JwtTokenProvider tokens, AuthenticationManager authenticationManager){
        this.users=users; this.encoder=encoder; this.tokens=tokens; this.authenticationManager=authenticationManager;
    }
    public LoginResponse login(LoginRequest request) {
        String email=request.email().trim().toLowerCase();
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));
        UserPrincipal principal=(UserPrincipal)authentication.getPrincipal();
        User user=users.findById(principal.getId()).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND","User account no longer exists."));
        return new LoginResponse(tokens.generateToken(authentication),"Bearer",tokens.getExpirationMs()/1000,UserResponse.from(user));
    }
    public UserResponse register(RegisterRequest request, String actorRole) {
        if (!"ROLE_ADMIN".equals(actorRole)) throw new AccessDeniedException("Only administrators can provision user accounts.");
        String email=request.email().trim().toLowerCase();
        if(users.existsByEmailIgnoreCase(email)) throw new ConflictException("EMAIL_EXISTS","Email is already assigned to a user.");
        User user=new User(); user.setEmail(email); user.setPasswordHash(encoder.encode(request.password()));
        Set<Role> roles=request.roles()==null || request.roles().isEmpty()?Set.of(Role.ROLE_EMPLOYEE):Set.copyOf(request.roles());
        user.setRoles(roles); user.setEmployeeId(request.employeeId()); user.setEnabled(true);
        return UserResponse.from(users.save(user));
    }
    public UserResponse getCurrentUser(UserPrincipal principal) {
        User user=users.findById(principal.getId()).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND","User account no longer exists."));
        return UserResponse.from(user);
    }
}
