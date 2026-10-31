package com.bit_rhythm.gateway.security;

import com.bit_rhythm.gateway.filter.HeaderOverrideRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Trust boundary for identity headers. Whatever the client sends as X-User-Id / X-User-Roles is
 * dropped; the gateway then sets them only from a JWT it has verified. Do NOT also remove these
 * headers in route filters (YAML), or the injected values would be stripped again.
 */
public class IdentityHeadersFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ROLES_HEADER = "X-User-Roles";
    public static final String USER_ID_ATTRIBUTE = IdentityHeadersFilter.class.getName() + ".userId";

    private static final Pattern SAFE_VALUE = Pattern.compile("[\\x20-\\x7E]{1,256}");

    private final String rolePrefix;

    public IdentityHeadersFilter(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Map<String, String> trusted = new LinkedHashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt && jwt.isAuthenticated()) {
            String subject = jwt.getToken().getSubject();
            if (subject != null && SAFE_VALUE.matcher(subject).matches()) {
                trusted.put(USER_ID_HEADER, subject);
                request.setAttribute(USER_ID_ATTRIBUTE, subject);
            }
            String roles = jwt.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(this::stripPrefix)
                    .filter(role -> SAFE_VALUE.matcher(role).matches() && role.indexOf(',') < 0)
                    .collect(Collectors.joining(","));
            if (!roles.isEmpty()) {
                trusted.put(ROLES_HEADER, roles);
            }
        }
        filterChain.doFilter(
                new HeaderOverrideRequest(request, Set.of(USER_ID_HEADER, ROLES_HEADER), trusted), response);
    }

    private String stripPrefix(String authority) {
        return !rolePrefix.isEmpty() && authority.startsWith(rolePrefix)
                ? authority.substring(rolePrefix.length()) : authority;
    }
}
