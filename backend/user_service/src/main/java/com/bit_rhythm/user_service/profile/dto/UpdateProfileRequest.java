package com.bit_rhythm.user_service.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String displayName,
        @Size(max = 500) String bio,
        @Size(min = 2, max = 2) String country) {}
