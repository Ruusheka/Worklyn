package com.worklyn.modules.auth.service;

import com.worklyn.common.security.UserPrincipal;
import com.worklyn.modules.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;
    public UserDetailsServiceImpl(UserRepository repository){this.repository=repository;}
    @Override public UserDetails loadUserByUsername(String email) {
        return repository.findByEmailIgnoreCase(email.trim()).map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
