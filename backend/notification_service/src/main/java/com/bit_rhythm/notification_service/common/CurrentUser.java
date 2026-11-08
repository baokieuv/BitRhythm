package com.bit_rhythm.notification_service.common;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

/** User id of the authenticated user: the "sub" claim of tokens issued by auth-service. */
public final class CurrentUser {

    private CurrentUser() {
    }

    /** Returns the id of the current user. */
    public static Long id(Jwt jwt) {
        return Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
    }
}
