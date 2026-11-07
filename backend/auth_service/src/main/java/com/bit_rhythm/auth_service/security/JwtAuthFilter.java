package com.bit_rhythm.auth_service.security;

import com.bit_rhythm.auth_service.common.util.ScopeUtils;
import com.bit_rhythm.auth_service.token.JwtService;
import com.bit_rhythm.auth_service.token.TokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads "Authorization: Bearer <jwt>" and authenticates the request from the token type:
 *
 * <ul>
 *   <li><b>access</b> (user): principal {@link AuthUser}; authorities ROLE_x, permissions, TOKEN_ACCESS</li>
 *   <li><b>service</b> (machine): principal {@link ServicePrincipal}; authorities SCOPE_x, TOKEN_SERVICE.
 *       The token must be addressed to this service (aud = auth.token.self-audience).</li>
 * </ul>
 *
 * Signature, issuer and expiry are checked once by {@link JwtService#verify(String)}. An invalid token is ignored:
 * the request stays anonymous and protected endpoints answer 401 through the authentication entry point.
 *
 * Not a @Bean on purpose: SecurityConfig creates it with "new", otherwise Spring Boot would also register it
 * as a servlet filter outside the security chain.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String TOKEN_AUTHORITY_PREFIX = "TOKEN_";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null
                && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Authentication authentication = authenticate(header.substring(BEARER_PREFIX.length()).trim());
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ex) {
                // Bad signature, expired, wrong issuer/audience, malformed, or a non-numeric subject: stay anonymous.
            }
        }

        chain.doFilter(request, response);
    }

    private Authentication authenticate(String token) {
        Claims claims = jwtService.verify(token);
        String tokenType = claims.get(TokenClaims.TOKEN_TYPE, String.class);

        if (TokenClaims.TYPE_ACCESS.equals(tokenType)) {
            jwtService.assertAccessToken(claims);
            return userAuthentication(claims);
        }
        if (TokenClaims.TYPE_SERVICE.equals(tokenType)) {
            jwtService.assertServiceToken(claims);
            return serviceAuthentication(claims);
        }
        return null;
    }

    private Authentication userAuthentication(Claims claims) {
        List<String> roles = stringList(claims, TokenClaims.ROLES);
        List<String> permissions = stringList(claims, TokenClaims.PERMISSIONS);

        AuthUser principal = new AuthUser(
                Long.parseLong(claims.getSubject()),
                claims.get(TokenClaims.EMAIL, String.class),
                roles,
                permissions);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(TOKEN_AUTHORITY_PREFIX + "ACCESS"));
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    private Authentication serviceAuthentication(Claims claims) {
        String clientId = claims.get(TokenClaims.CLIENT_ID, String.class);
        if (clientId == null) {
            clientId = claims.getSubject();
        }
        List<String> scopes = ScopeUtils.parse(claims.get(TokenClaims.SCOPE, String.class));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(TOKEN_AUTHORITY_PREFIX + "SERVICE"));
        scopes.forEach(scope -> authorities.add(new SimpleGrantedAuthority(scope)));

        return new UsernamePasswordAuthenticationToken(new ServicePrincipal(clientId, scopes), null, authorities);
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Claims claims, String name) {
        List<String> values = claims.get(name, List.class);
        return values == null ? List.of() : values;
    }
}