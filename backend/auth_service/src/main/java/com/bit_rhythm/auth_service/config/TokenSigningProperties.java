package com.bit_rhythm.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * Asymmetric (RS256) token signing settings.
 *
 * <pre>
 * auth:
 *   token:
 *     issuer: <a href="http://auth-service">http://auth-service</a>        # must be identical in every service that validates tokens
 *     user-audience: bit-rhythm-api
 *     self-audience: auth-service        # "aud" a service token must carry to call THIS service
 *     key-id: auth-key-1
 *     private-key: ${JWT_PRIVATE_KEY}    # PKCS#8 PEM
 *     public-key: ${JWT_PUBLIC_KEY}      # X.509 PEM
 *     retired-keys:                      # old public keys kept in JWKS while old tokens are still alive
 *       - key-id: auth-key-0
 *         public-key: ...
 * </pre>
 */
@ConfigurationProperties(prefix = "auth.token")
public record TokenSigningProperties(
        @DefaultValue("http://auth-service") String issuer,
        @DefaultValue("bit-rhythm-api") String userAudience,
        @DefaultValue("auth-service") String selfAudience,
        @DefaultValue("5m") Duration serviceTokenTtl,
        @DefaultValue("30s") Duration clockSkew,
        @DefaultValue("auth-key-1") String keyId,
        String privateKey,
        String publicKey,
        List<RetiredKey> retiredKeys
) {
    public TokenSigningProperties {
        if (retiredKeys == null) {
            retiredKeys = List.of();
        }
    }

    public record RetiredKey(String keyId, String publicKey) {
    }
}