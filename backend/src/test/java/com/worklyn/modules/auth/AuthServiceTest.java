package com.worklyn.modules.auth;

import com.worklyn.common.exception.ConflictException;
import com.worklyn.common.security.JwtTokenProvider;
import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.auth.dto.*;
import com.worklyn.modules.auth.model.Role;
import com.worklyn.modules.auth.model.User;
import com.worklyn.modules.auth.repository.UserRepository;
import com.worklyn.modules.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService service(UserRepository repo, PasswordEncoder encoder,
                                JwtTokenProvider tokens, AuthenticationManager manager) {
        return new AuthService(repo, encoder, tokens, manager);
    }

    @Test
    void registerRejectsNonAdmin() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AuthService service = service(repo, encoder, mock(JwtTokenProvider.class),
                                      mock(AuthenticationManager.class));
        RegisterRequest req = new RegisterRequest(
                "a@example.com", "a-very-strong-password",
                Set.of(Role.ROLE_EMPLOYEE), null);

        assertThrows(AccessDeniedException.class, () -> service.register(req, "ROLE_HR"));
        verifyNoInteractions(repo, encoder);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.existsByEmailIgnoreCase("a@example.com")).thenReturn(true);
        AuthService service = service(repo, mock(PasswordEncoder.class),
                mock(JwtTokenProvider.class), mock(AuthenticationManager.class));
        RegisterRequest req = new RegisterRequest(
                "a@example.com", "a-very-strong-password",
                Set.of(Role.ROLE_EMPLOYEE), null);

        assertThrows(ConflictException.class, () -> service.register(req, "ROLE_ADMIN"));
        verify(repo, never()).save(any());
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndSavesUser() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.existsByEmailIgnoreCase("a@example.com")).thenReturn(false);
        when(encoder.encode("a-very-strong-password")).thenReturn("bcrypt-hash");
        when(repo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("user-1");
            return u;
        });

        AuthService service = service(repo, encoder, mock(JwtTokenProvider.class),
                                      mock(AuthenticationManager.class));
        RegisterRequest req = new RegisterRequest(
                " A@Example.com ", "a-very-strong-password",
                Set.of(Role.ROLE_EMPLOYEE), null);

        UserResponse result = service.register(req, "ROLE_ADMIN");

        assertEquals("user-1", result.id());
        assertEquals("a@example.com", result.email());
        assertTrue(result.roles().contains(Role.ROLE_EMPLOYEE));
        verify(encoder).encode("a-very-strong-password");
        verify(repo).save(argThat(u -> u.getEmail().equals("a@example.com")
                && u.getPasswordHash().equals("bcrypt-hash") && u.isEnabled()));
    }

    @Test
    void registerDefaultsToEmployeeRoleWhenRolesAreEmpty() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repo.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthService service = service(repo, encoder, mock(JwtTokenProvider.class),
                                      mock(AuthenticationManager.class));
        RegisterRequest req = new RegisterRequest(
                "a@example.com", "a-very-strong-password", Set.of(), null);

        assertTrue(service.register(req, "ROLE_ADMIN").roles().contains(Role.ROLE_EMPLOYEE));
    }

    @Test
    void loginReturnsBearerTokenAndUser() {
        UserRepository repo = mock(UserRepository.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        JwtTokenProvider tokens = mock(JwtTokenProvider.class);
        User user = new User();
        user.setId("user-1");
        user.setEmail("a@example.com");
        user.setRoles(Set.of(Role.ROLE_EMPLOYEE));

        UserPrincipal principal = new UserPrincipal(
                "user-1", "a@example.com", "hash", null,
                Set.of(Role.ROLE_EMPLOYEE), true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(manager.authenticate(any())).thenReturn(auth);
        when(repo.findById("user-1")).thenReturn(Optional.of(user));
        when(tokens.generateToken(auth)).thenReturn("jwt-token");
        when(tokens.getExpirationMs()).thenReturn(86400000L);

        AuthService service = service(repo, mock(PasswordEncoder.class), tokens, manager);
        LoginResponse result = service.login(new LoginRequest(" A@Example.com ", "password"));

        assertEquals("jwt-token", result.token());
        assertEquals("Bearer", result.tokenType());
        assertEquals(86400L, result.expiresIn());
        assertEquals("a@example.com", result.user().email());
    }

    @Test
    void loginPropagatesBadCredentials() {
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        AuthService service = service(mock(UserRepository.class), mock(PasswordEncoder.class),
                mock(JwtTokenProvider.class), manager);

        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("a@example.com", "wrong-password")));
    }

    @Test
    void currentUserReturnsSafeProfile() {
        UserRepository repo = mock(UserRepository.class);
        User user = new User();
        user.setId("user-1");
        user.setEmail("a@example.com");
        user.setPasswordHash("must-not-be-returned");
        user.setRoles(Set.of(Role.ROLE_EMPLOYEE));
        when(repo.findById("user-1")).thenReturn(Optional.of(user));

        AuthService service = service(repo, mock(PasswordEncoder.class),
                mock(JwtTokenProvider.class), mock(AuthenticationManager.class));
        UserPrincipal principal = new UserPrincipal(
                "user-1", "a@example.com", "hash", null,
                Set.of(Role.ROLE_EMPLOYEE), true);

        UserResponse result = service.getCurrentUser(principal);

        assertEquals("user-1", result.id());
        assertEquals("a@example.com", result.email());
        assertFalse(result.toString().contains("must-not-be-returned"));
    }
}
