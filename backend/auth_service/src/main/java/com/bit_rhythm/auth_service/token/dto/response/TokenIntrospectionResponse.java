package com.bit_rhythm.auth_service.token.dto.response;

import java.time.Instant;

public record TokenIntrospectionResponse(
        boolean active,
        String clientId,
        String tokenType,
        String scope,
        Instant issuedAt,
        Instant expiresAt
) {
    public static TokenIntrospectionResponse inactive() {
        return new TokenIntrospectionResponse(false, null, null, null, null, null);
    }
}
