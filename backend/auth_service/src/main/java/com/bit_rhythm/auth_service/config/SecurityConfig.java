package com.bit_rhythm.auth_service.config;

import com.bit_rhythm.auth_service.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, RateLimitFilter rateLimitFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/auth/register", "/auth/login", "/auth/login/mfa", "/auth/refresh",
                                "/auth/verify-email", "/auth/resend-verification",
                                "/auth/forgot-password", "/auth/reset-password",
                                "/auth/service-token", "/.well-known/jwks.json",
                                "/health/**").permitAll()
                        .requestMatchers("/auth/introspect", "/auth/authorize").hasAuthority("SCOPE_internal")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtAuthConverter())))
                .addFilterBefore(rateLimitFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    // maps claims "permissions" -> authorities (user:read), "roles" -> ROLE_ADMIN, "scope" -> SCOPE_internal
    @Bean
    JwtAuthenticationConverter jwtAuthConverter() { /* ... */ }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 1 << 16, 3);
    }
}
