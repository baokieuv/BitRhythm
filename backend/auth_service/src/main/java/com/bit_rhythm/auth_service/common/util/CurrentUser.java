package com.bit_rhythm.auth_service.common.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

public final class CurrentUser {
    private CurrentUser() {}

    public static Long id(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }

    public static String email(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }
}
