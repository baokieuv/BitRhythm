package com.bit_rhythm.auth_service.token.dto.response;

public record ServiceTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String scope
) {
}
