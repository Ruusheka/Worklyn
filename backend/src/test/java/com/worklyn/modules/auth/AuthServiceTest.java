package com.worklyn.modules.auth;

import com.worklyn.common.security.JwtTokenProvider;
import com.worklyn.modules.auth.dto.RegisterRequest;
import com.worklyn.modules.auth.model.Role;
import com.worklyn.modules.auth.model.User;
import com.worklyn.modules.auth.repository.UserRepository;
import com.worklyn.modules.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
 @Test void registerRejectsNonAdmin() {
  UserRepository repo=mock(UserRepository.class); PasswordEncoder encoder=mock(PasswordEncoder.class);
  AuthService service=new AuthService(repo,encoder,mock(JwtTokenProvider.class),mock(AuthenticationManager.class));
  RegisterRequest req=new RegisterRequest("a@example.com","a-very-strong-password",Set.of(Role.ROLE_EMPLOYEE),null);
  assertThrows(AccessDeniedException.class,()->service.register(req,"ROLE_HR")); verifyNoInteractions(repo,encoder);
 }
 @Test void registerRejectsDuplicateEmail() {
  UserRepository repo=mock(UserRepository.class); when(repo.existsByEmailIgnoreCase("a@example.com")).thenReturn(true);
  AuthService service=new AuthService(repo,mock(PasswordEncoder.class),mock(JwtTokenProvider.class),mock(AuthenticationManager.class));
  RegisterRequest req=new RegisterRequest("a@example.com","a-very-strong-password",Set.of(Role.ROLE_EMPLOYEE),null);
  assertThrows(com.worklyn.common.exception.ConflictException.class,()->service.register(req,"ROLE_ADMIN"));
 }
}
