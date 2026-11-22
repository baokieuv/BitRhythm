package com.bit_rhythm.content_service.album.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** coverKey comes from POST /studio/covers/upload-url (already uploaded); null keeps the current cover. */
public record AlbumRequest(
        @NotBlank @Size(max = 200) String title,
        LocalDate releaseDate,
        String coverKey
) {
}
