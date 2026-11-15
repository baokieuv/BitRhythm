package com.bit_rhythm.content_service.playlist.dto;

import com.bit_rhythm.content_service.playlist.PlaylistVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaylistRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull PlaylistVisibility visibility
) {
}
