package com.bit_rhythm.user_service.artist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitVerificationRequest(@NotBlank @Size(max = 1000) String message) {
}
