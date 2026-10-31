package com.bit_rhythm.gateway.security;

import com.bit_rhythm.gateway.config.GatewayProperties;
import com.bit_rhythm.gateway.error.ProblemResponses;
import com.bit_rhythm.gateway.ratelimit.RateLimitFilter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayProperties properties,
                                            MeterRegistry meters) throws Exception {
        GatewayProperties.Security security = properties.security();

        AuthenticationEntryPoint entryPoint = (request, response, exception) -> {
            boolean hadToken = request.getHeader(HttpHeaders.AUTHORIZATION) != null;
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, hadToken ? "Bearer error=\"invalid_token\"" : "Bearer");
            ProblemResponses.write(request, response, HttpStatus.UNAUTHORIZED,
                    hadToken ? "Invalid or expired access token" : "Authentication required");
        };
        AccessDeniedHandler accessDenied = (request, response, exception) ->
                ProblemResponses.write(request, response, HttpStatus.FORBIDDEN, "Access denied");

        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(security.rolesClaim());
        authorities.setAuthorityPrefix(security.rolePrefix());
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authorities);

        List<IpAddressMatcher> metricsNetworks = security.metricsAllowedCidrs().stream()
                .map(IpAddressMatcher::new).toList();
        AuthorizationManager<RequestAuthorizationContext> metricsAccess = (authentication, context) ->
                new AuthorizationDecision(metricsNetworks.stream().anyMatch(m -> m.matches(context.getRequest())));

        PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsSource(properties.cors())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                    auth.requestMatchers(paths.matcher(HttpMethod.OPTIONS, "/**")).permitAll();
                    auth.requestMatchers(paths.matcher("/health/**")).permitAll();
                    auth.requestMatchers(paths.matcher("/prometheus"), paths.matcher("/info")).access(metricsAccess);
                    // Never expose upstream internal/actuator endpoints through the gateway.
                    auth.requestMatchers(paths.matcher("/api/*/internal/**"),
                            paths.matcher("/api/*/actuator/**")).denyAll();
                    for (String endpoint : security.publicEndpoints()) {
                        auth.requestMatchers(toMatcher(paths, endpoint)).permitAll();
                    }
                    auth.requestMatchers(paths.matcher("/api/*/admin/**")).hasRole(security.adminRole());
                    auth.requestMatchers(paths.matcher("/api/**")).authenticated();
                    auth.anyRequest().denyAll();
                })
                .oauth2ResourceServer(resource -> resource
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDenied))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDenied))
                // Order matters: authenticate (JWT) -> inject trusted identity headers -> rate limit -> authorize.
                .addFilterAfter(new IdentityHeadersFilter(security.rolePrefix()), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new RateLimitFilter(properties.rateLimit(), meters), IdentityHeadersFilter.class);

        return http.build();
    }

    private static RequestMatcher toMatcher(PathPatternRequestMatcher.Builder paths, String endpoint) {
        String[] parts = endpoint.trim().split("\\s+", 2);
        if (parts.length == 2) {
            return paths.matcher(HttpMethod.valueOf(parts[0].toUpperCase()), parts[1]);
        }
        return paths.matcher(parts[0]);
    }

    private static CorsConfigurationSource corsSource(GatewayProperties.Cors cors) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(cors.allowedOrigins());
        configuration.setAllowedMethods(cors.allowedMethods());
        configuration.setAllowedHeaders(cors.allowedHeaders());
        configuration.setAllowCredentials(cors.allowCredentials());
        configuration.setMaxAge(cors.maxAge());
        configuration.setExposedHeaders(List.of("X-Correlation-Id", "Retry-After",
                "X-RateLimit-Limit", "X-RateLimit-Remaining"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
