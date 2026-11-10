package com.bit_rhythm.user_service.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String displayName,
        @Size(max = 500) String bio,
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "must be a 2-letter country code") String country
) {
}
