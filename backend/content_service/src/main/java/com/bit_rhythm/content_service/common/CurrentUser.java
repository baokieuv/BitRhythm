package com.bit_rhythm.content_service.common;

import org.springframework.security.oauth2.jwt.Jwt;

/** User id of the authenticated user: the "sub" claim of tokens issued by auth-service. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
