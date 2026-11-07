package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.config.TokenSigningProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * Holds the RSA key used to sign tokens plus every public key that may still be needed to verify them
 * (the active one first, then retired ones). Exposes the public keys as a JWK Set.
 */
@Slf4j
@Service
public class SigningKeyService {
    @Getter
    private final String activeKeyId;
    @Getter
    private final PrivateKey signingKey;
    private final Map<String, RSAPublicKey> verificationKeys = new LinkedHashMap<>();

    public SigningKeyService(TokenSigningProperties properties) {
        this.activeKeyId = properties.keyId();

        RSAPublicKey activePublicKey;
        if (hasText(properties.privateKey()) && hasText(properties.publicKey())) {
            this.signingKey = parsePrivateKey(properties.privateKey());
            activePublicKey = (RSAPublicKey) parsePublicKey(properties.publicKey());
            assertKeyPair(this.signingKey, activePublicKey);
        } else {
            log.warn("No active public key configured; using the signing key as the public key");
            KeyPair pair = generateKeyPair();
            this.signingKey = pair.getPrivate();
            activePublicKey = (RSAPublicKey) pair.getPublic();
        }

        verificationKeys.put(activeKeyId, activePublicKey);
        for (TokenSigningProperties.RetiredKey retired : properties.retiredKeys()) {
            verificationKeys.putIfAbsent(retired.keyId(), (RSAPublicKey) parsePublicKey(retired.publicKey()));
        }
    }

    public Optional<PublicKey> findVerificationKey(String keyId) {
        return Optional.ofNullable(verificationKeys.get(keyId));
    }

    /** JWK Set (RFC 7517) containing every public key that can verify our tokens. */
    public Map<String, Object> jwks() {
        List<Map<String, Object>> keys = verificationKeys.entrySet().stream()
                .map(entry -> toJwk(entry.getKey(), entry.getValue()))
                .toList();
        return Map.of("keys", keys);
    }

    private static Map<String, Object> toJwk(String keyId, RSAPublicKey key) {
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", keyId);
        jwk.put("n", base64Url(key.getModulus()));
        jwk.put("e", base64Url(key.getPublicExponent()));
        return jwk;
    }

    private static String base64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) { // drop the sign byte
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static PrivateKey parsePrivateKey(String pem) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodePem(pem)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse private key", e);
        }
    }

    private static PublicKey parsePublicKey(String pem) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodePem(pem)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse public key", e);
        }
    }

    private static void assertKeyPair(PrivateKey privateKey, RSAPublicKey publicKey) {
        if (privateKey instanceof RSAPrivateKey rsaPrivateKey
                && !rsaPrivateKey.getModulus().equals(publicKey.getModulus())) {
            throw new IllegalStateException("auth.token.private-key and auth.token.public-key do not match");
        }
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot generate RSA key pair", e);
        }
    }

    private static byte[] decodePem(String pem) {
        String body = pem
                .replace("\\n", "") // keys passed through env vars often contain literal \n
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(body);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
