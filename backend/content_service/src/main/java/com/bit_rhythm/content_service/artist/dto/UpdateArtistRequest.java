package com.bit_rhythm.content_service.artist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** imageKey comes from POST /studio/covers/upload-url (the file is already uploaded); null keeps the current image. */
public record UpdateArtistRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String bio,
        String imageKey
) {
}
