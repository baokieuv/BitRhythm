package com.bit_rhythm.auth_service.token.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ServiceTokenRequest(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        String scope
) {
}
