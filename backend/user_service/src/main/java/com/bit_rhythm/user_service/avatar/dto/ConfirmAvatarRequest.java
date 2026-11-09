package com.bit_rhythm.user_service.avatar.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmAvatarRequest(@NotBlank String key) {}
