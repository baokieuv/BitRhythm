package com.bit_rhythm.auth_service.token.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenIntrospectionRequest(@NotBlank String token) {
}
