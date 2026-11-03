package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.rbac.Permission;
import com.bit_rhythm.auth_service.rbac.Role;
import com.bit_rhythm.auth_service.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Issues and parses access tokens. HMAC-signed (HS256 family) with the single secret from jwt.secret.
 * Claims: sub = user id, email, roles, iat, exp.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // Throws a clear WeakKeyException at startup if the secret is shorter than 256 bits.
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        List<String> permissionNames = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roleNames)
                .claim("permissions", permissionNames)
                .claim("token_type", "access")
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenTtl())))
                .signWith(key)
                .compact();
    }

    /**
     * Verifies signature and expiry and returns the claims.
     * Throws a JwtException (or IllegalArgumentException for a blank token) if the token is not valid.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public ServiceToken generateServiceToken(String clientId, List<String> scopes, List<String> audiences) {
        return null;
    }

    public record ServiceToken(String value, Instant issuedAt, Instant expiresAt) {
        public long expiresInSeconds() {
            return Math.max(1, expiresAt.getEpochSecond() - issuedAt.getEpochSecond());
        }
    }
}