package com.bit_rhythm.auth_service.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenGenerator() {
    }

    public static String generateRefreshToken() {
        byte[] value = new byte[64];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public static long generateFamilyId() {
        long value = RANDOM.nextLong() & Long.MAX_VALUE;
        return value == 0 ? 1 : value;
    }
}
