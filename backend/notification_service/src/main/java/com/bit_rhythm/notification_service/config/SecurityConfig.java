package com.bit_rhythm.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The JwtDecoder (JWKS, issuer, audiences) is built by Spring Boot from spring.security.oauth2.resourceserver.jwt.*.
 * Authorities: TOKEN_ACCESS / TOKEN_SERVICE, SCOPE_x (service tokens), ROLE_x and permissions (user tokens).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health/**", "/error").permitAll()
                        // links inside emails and provider callbacks carry no bearer token
                        .requestMatchers("/internal/**").hasAuthority("TOKEN_SERVICE")  // + @PreAuthorize on scope
                        .requestMatchers("/api/notifications/**").hasAuthority("TOKEN_ACCESS")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopeConverter.convert(jwt));

            String tokenType = jwt.getClaimAsString("token_type");
            if (tokenType != null) {
                authorities.add(new SimpleGrantedAuthority("TOKEN_" + tokenType.toUpperCase(Locale.ROOT)));
            }
            addAll(authorities, jwt.getClaimAsStringList("roles"), "ROLE_");
            addAll(authorities, jwt.getClaimAsStringList("permissions"), "");
            addAll(authorities, jwt.getClaimAsStringList("scopes"), "");
            return authorities;
        });
        return converter;
    }

    private void addAll(Set<GrantedAuthority> target, List<String> values, String prefix) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            target.add(new SimpleGrantedAuthority(value.startsWith(prefix) ? value : prefix + value));
        }
    }
}
