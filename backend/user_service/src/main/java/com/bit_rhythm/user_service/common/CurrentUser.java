package com.bit_rhythm.user_service.common;

import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {
    private CurrentUser() {}

    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    public static String email(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }
}
