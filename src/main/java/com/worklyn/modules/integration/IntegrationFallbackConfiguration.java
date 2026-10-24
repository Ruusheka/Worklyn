package com.worklyn.modules.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fallback configuration providing dummy/default implementations for dependencies
 * owned by Person 1 (EmployeeDirectory) and Person 3 (WorkflowRecoveryPort)
 * when their actual components are not present in the application context.
 */
@Configuration
public class IntegrationFallbackConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmployeeDirectory.class)
    public EmployeeDirectory defaultEmployeeDirectory() {
        return new DefaultEmployeeDirectory();
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowRecoveryPort.class)
    public WorkflowRecoveryPort noOpWorkflowRecoveryPort() {
        return new NoOpWorkflowRecoveryPort();
    }

    @Bean
    @ConditionalOnMissingBean(org.springframework.security.web.SecurityFilterChain.class)
    public org.springframework.security.web.SecurityFilterChain fallbackSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
