package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.util.ScopeUtils;
import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.config.TokenSigningProperties;
import com.bit_rhythm.auth_service.rbac.Permission;
import com.bit_rhythm.auth_service.rbac.Role;
import com.bit_rhythm.auth_service.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ProtectedHeader;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * The single place that signs and parses tokens (RS256, key id in the header).
 *
 * <ul>
 *   <li>access token (user): sub = user id, aud = user-audience, token_type = access</li>
 *   <li>service token (machine): sub = client_id, aud = target service(s), scope, token_type = service</li>
 * </ul>
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

    // issuing

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

    // parsing

    /**
     * Verifies signature, issuer and expiry only. Used by introspection, which accepts any token type.
     * Throws JwtException (or IllegalArgumentException for a blank token) when invalid.
     */
    public Claims verify(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    /** For callers that already ran {@link #verify(String)} and looked at token_type. Throws JwtException. */
    public void assertAccessToken(Claims claims) {
        requireTokenType(claims, TokenClaims.TYPE_ACCESS);
        requireAudience(claims, signingProperties.userAudience());
    }

    /** For callers that already ran {@link #verify(String)} and looked at token_type. Throws JwtException. */
    public void assertServiceToken(Claims claims) {
        requireTokenType(claims, TokenClaims.TYPE_SERVICE);
        requireAudience(claims, signingProperties.selfAudience());
    }

    // internals

    private Key locateVerificationKey(Header header) {
        String keyId = header instanceof ProtectedHeader protectedHeader ? protectedHeader.getKeyId() : null;
        return signingKeys.findVerificationKey(keyId)
                .orElseThrow(() -> new JwtException("Unknown signing key id"));
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

    public record ServiceToken(String value, Instant issuedAt, Instant expiresAt) {
        public long expiresInSeconds() {
            return Math.max(1, expiresAt.getEpochSecond() - issuedAt.getEpochSecond());
        }
    }
}