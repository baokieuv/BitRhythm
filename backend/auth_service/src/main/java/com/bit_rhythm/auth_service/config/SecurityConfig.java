package com.bit_rhythm.auth_service.config;

import com.bit_rhythm.auth_service.security.JwtAuthFilter;
import com.bit_rhythm.auth_service.security.RateLimitFilter;
import com.bit_rhythm.auth_service.token.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, AppProperties.class, TokenSigningProperties.class})
public class SecurityConfig {

    /** Authority added by JwtAuthFilter to user (access) tokens. */
    private static final String USER_TOKEN = "TOKEN_ACCESS";
    /** Authority added by JwtAuthFilter to service (machine-to-machine) tokens. */
    private static final String SERVICE_TOKEN = "TOKEN_SERVICE";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Not used (we authenticate with JWT), but declaring it stops Spring Boot from creating a default
     * user with a generated password. Works on every Spring Boot version, unlike an autoconfigure exclude.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Username/password login is not supported here");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   AppProperties appProperties) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Rules are evaluated top to bottom, the first match wins.
                .authorizeHttpRequests(auth -> auth
                        // --- public ---------------------------------------------------------
                        .requestMatchers(HttpMethod.POST,
                                "/auth/register",
                                "/auth/verify-email",
                                "/auth/resend-verification",
                                "/auth/request-password-reset",
                                "/auth/reset-password",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                // OAuth2 endpoints authenticate the calling service themselves
                                // (client_id/client_secret), so they must not require a bearer token.
                                "/token",
                                "/token/introspect").permitAll()
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        .requestMatchers("/health/**", "/error").permitAll()
                        // --- user tokens only -------------------------------------------------
                        .requestMatchers("/auth/me", "/auth/change-password", "/auth/logout-all")
                        .hasAuthority(USER_TOKEN)
                        // --- service tokens only ----------------------------------------------
                        // Add fine-grained checks per endpoint, e.g. @PreAuthorize("hasAuthority('SCOPE_users:read')").
                        .requestMatchers("/internal/**").hasAuthority(SERVICE_TOKEN)
                        // --- everything else (RBAC/admin APIs, ...): user tokens only ---------
                        // Safe default: a service token cannot reach an endpoint unless it is listed above.
                        .anyRequest().hasAuthority(USER_TOKEN))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeError(req, res, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required"))
                        .accessDeniedHandler((req, res, e) ->
                                writeError(req, res, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied")))
                // Filters are created with "new" on purpose: a Filter declared as a bean would ALSO be
                // registered in the servlet container, outside the security chain.
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        AppProperties.RateLimit rateLimit = appProperties.rateLimit();
        if (rateLimit != null && rateLimit.enabled() && !rateLimit.rules().isEmpty()) {
            // Runs before the JWT filter, so abusive requests are rejected as early as possible.
            http.addFilterBefore(new RateLimitFilter(rateLimit.rules()), JwtAuthFilter.class);
        }

        return http.build();
    }

    /** Same shape as ApiError. Written by hand so it does not depend on which Jackson version Spring uses. */
    private static void writeError(HttpServletRequest req, HttpServletResponse res,
                                   HttpStatus status, String code, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = """
                {"timestamp":"%s","status":%d,"code":"%s","message":"%s","path":"%s"}"""
                .formatted(Instant.now(), status.value(), code, message, escape(req.getRequestURI()));
        res.getWriter().write(body);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}