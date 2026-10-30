package com.bit_rhythm.auth_service.verification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerificationRequest(
        @NotBlank String token
) {
}
