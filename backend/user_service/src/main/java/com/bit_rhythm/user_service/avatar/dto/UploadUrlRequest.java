package com.bit_rhythm.user_service.avatar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UploadUrlRequest(@NotBlank String contentType, @Positive long size) {}
