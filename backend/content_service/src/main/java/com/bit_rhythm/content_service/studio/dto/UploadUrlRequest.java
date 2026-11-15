package com.bit_rhythm.content_service.studio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UploadUrlRequest(@NotBlank String contentType, @Min(1) long sizeBytes) {
}
