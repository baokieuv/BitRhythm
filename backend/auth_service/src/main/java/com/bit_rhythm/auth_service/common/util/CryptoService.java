package com.bit_rhythm.auth_service.common.util;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Service
public class CryptoService {

    public String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }
        return HashUtil.sha256(token);
    }

    public boolean matches(String rawToken, String hashedToken) {
        if (rawToken == null || hashedToken == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hashToken(rawToken).getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                hashedToken.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }
}
