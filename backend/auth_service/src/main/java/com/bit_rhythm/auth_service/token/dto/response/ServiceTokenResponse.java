package com.bit_rhythm.auth_service.token.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ServiceTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("scope") String scope
) {
}
