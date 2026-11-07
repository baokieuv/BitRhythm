package com.bit_rhythm.auth_service.token.dto.response;

public record CreatedClient(
        String clientId,
        String clientSecret
) {
}
