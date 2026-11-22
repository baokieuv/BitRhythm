package com.bit_rhythm.content_service.track.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Metadata of a track (create and update). coverKey comes from POST /studio/covers/upload-url (already uploaded); null keeps the
 * current cover. The audio is NOT part of this request: it has its own upload flow.
 */
public record TrackRequest(
        @NotBlank @Size(max = 200) String title,
        Long albumId,
        @Min(1) Integer trackNumber,
        Long genreId,
        boolean explicitContent,
        String coverKey
) {
}
