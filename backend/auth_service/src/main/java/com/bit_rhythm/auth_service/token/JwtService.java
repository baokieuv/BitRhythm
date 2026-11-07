package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.util.ScopeUtils;
import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.config.TokenSigningProperties;
import com.bit_rhythm.auth_service.rbac.Permission;
import com.bit_rhythm.auth_service.rbac.Role;
import com.bit_rhythm.auth_service.user.User;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Issues and parses access tokens. HMAC-signed (HS256 family) with the single secret from jwt.secret.
 * Claims: sub = user id, email, roles, iat, exp.
 */
@Service
public class JwtService {

    private final SigningKeyService signingKeys;
    private final JwtProperties jwtProperties;
    private final TokenSigningProperties signingProperties;
    private final JwtParser parser;

    public JwtService(SigningKeyService signingKeys,
                      JwtProperties jwtProperties,
                      TokenSigningProperties signingProperties) {
        this.signingKeys = signingKeys;
        this.jwtProperties = jwtProperties;
        this.signingProperties = signingProperties;
        this.parser = Jwts.parser()
                .keyLocator(this::locateVerificationKey)
                .requireIssuer(signingProperties.issuer())
                .clockSkewSeconds(signingProperties.clockSkew().toSeconds())
                .build();
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
                .header().keyId(signingKeys.getActiveKeyId()).and()
                .issuer(signingProperties.issuer())
                .subject(user.getId().toString())
                .audience().add(signingProperties.userAudience()).and()
                .claim(TokenClaims.EMAIL, user.getEmail())
                .claim(TokenClaims.ROLES, roleNames)
                .claim(TokenClaims.PERMISSIONS, permissionNames)
                .claim(TokenClaims.TOKEN_TYPE, TokenClaims.TYPE_ACCESS)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.accessTokenTtl())))
                .signWith(signingKeys.getSigningKey(), Jwts.SIG.RS256)
                .compact();
    }

    public ServiceToken generateServiceToken(String clientId, Collection<String> scopes, Collection<String> audiences) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(signingProperties.serviceTokenTtl());

        String token = Jwts.builder()
                .header().keyId(signingKeys.getActiveKeyId()).and()
                .issuer(signingProperties.issuer())
                .subject(clientId)
                .audience().add(audiences).and()
                .claim(TokenClaims.CLIENT_ID, clientId)
                .claim(TokenClaims.SCOPE, ScopeUtils.join(scopes))
                .claim(TokenClaims.TOKEN_TYPE, TokenClaims.TYPE_SERVICE)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKeys.getSigningKey(), Jwts.SIG.RS256)
                .compact();

        return new ServiceToken(token, now, expiresAt);
    }

    /**
     * Verifies signature, issuer and expiry only. Used by introspection, which accepts any token type.
     * Throws JwtException (or IllegalArgumentException for a blank token) when invalid.
     */
    public Claims verify(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }


    /** Parses a USER access token: also requires token_type = access and the user audience. */
    public Claims parse(String token) {
        Claims claims = verify(token);
        requireTokenType(claims, TokenClaims.TYPE_ACCESS);
        requireAudience(claims, signingProperties.userAudience());
        return claims;
    }

    /** Parses a SERVICE token: also requires token_type = service. Audience is checked by the receiving service. */
    public Claims parseServiceToken(String token) {
        Claims claims = verify(token);
        requireTokenType(claims, TokenClaims.TYPE_SERVICE);
        return claims;
    }

    private void requireTokenType(Claims claims, String expected) {
        if (!expected.equals(claims.get(TokenClaims.TOKEN_TYPE, String.class))) {
            throw new JwtException("Unexpected token type");
        }
    }

    private void requireAudience(Claims claims, String expected) {
        if (claims.getAudience() == null || !claims.getAudience().contains(expected)) {
            throw new JwtException("Unexpected token audience");
        }
    }

    private Key locateVerificationKey(Header header) {
        String keyId = header instanceof ProtectedHeader protectedHeader ? protectedHeader.getKeyId() : null;
        return signingKeys.findVerificationKey(keyId)
                .orElseThrow(() -> new JwtException("Unknown signing key id"));
    }

    public record ServiceToken(String value, Instant issuedAt, Instant expiresAt) {
        public long expiresInSeconds() {
            return Math.max(1, expiresAt.getEpochSecond() - issuedAt.getEpochSecond());
        }
    }
}