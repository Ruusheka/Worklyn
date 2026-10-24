package com.worklyn.common.security;

import com.worklyn.modules.auth.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final RestAuthenticationEntryPoint entryPoint;
    private final RestAccessDeniedHandler deniedHandler;
    private final UserDetailsServiceImpl userDetailsService;
    public SecurityConfig(JwtAuthenticationFilter jwtFilter, RestAuthenticationEntryPoint entryPoint,
                          RestAccessDeniedHandler deniedHandler, UserDetailsServiceImpl userDetailsService) {
        this.jwtFilter=jwtFilter; this.entryPoint=entryPoint; this.deniedHandler=deniedHandler; this.userDetailsService=userDetailsService;
    }
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean DaoAuthenticationProvider authenticationProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider(); provider.setUserDetailsService(userDetailsService); provider.setPasswordEncoder(encoder); return provider;
    }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> {}).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
            .authenticationProvider(provider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/api/v1/auth/login", "/s/**").permitAll()
                .requestMatchers("/api/v1/auth/register").hasRole("ADMIN")
                .requestMatchers("/api/v1/auth/me").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/employees").hasAnyRole("ADMIN","HR")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/employees/me").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/employees/me").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/employees/**").hasAnyRole("ADMIN","HR")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/employees/**").hasAnyRole("ADMIN","HR")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/employees").hasAnyRole("ADMIN","HR","MANAGER")
                                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/employees/**").authenticated()
                .requestMatchers("/api/v1/crawler/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/rewind/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/system/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/shortlinks/**").authenticated()
                .requestMatchers("/api/v1/autocomplete/**").authenticated()
                .requestMatchers("/api/v1/**").authenticated().anyRequest().denyAll())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
