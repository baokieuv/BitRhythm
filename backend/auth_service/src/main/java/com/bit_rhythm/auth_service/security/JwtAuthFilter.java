package com.bit_rhythm.auth_service.security;

import com.bit_rhythm.auth_service.token.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads "Authorization: Bearer <jwt>" and, if the token is valid, authenticates the request with an
 * {@link AuthUser} principal (id, email, roles). An invalid or expired token is simply ignored: the request
 * stays anonymous, and protected endpoints answer 401 through the authentication entry point in SecurityConfig.
 *
 * Not a @Bean on purpose: SecurityConfig creates it with "new", otherwise Spring Boot would also register it
 * as a servlet filter outside the security chain.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null
                && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parse(header.substring(BEARER_PREFIX.length()).trim());
                if (!"access".equals(claims.get("token_type", String.class))) {
                    chain.doFilter(request, response);
                    return;
                }

                // Extract roles list from claims
                List<String> roles = claims.get("roles", List.class);
                List<String> permissionList = claims.get("permissions", List.class);
                roles = roles == null ? List.of() : roles;
                permissionList = permissionList == null ? List.of() : permissionList;

                AuthUser principal = new AuthUser(
                        Long.parseLong(claims.getSubject()),
                        claims.get("email", String.class),
                        roles,
                        permissionList); // Updated to pass the list of roles

                // Map string roles to Spring Security SimpleGrantedAuthority objects
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities));
            } catch (JwtException | IllegalArgumentException ex) {
                // Bad signature, expired, malformed, or a subject that is not a UUID: stay unauthenticated.
            }
        }

        chain.doFilter(request, response);
    }
}