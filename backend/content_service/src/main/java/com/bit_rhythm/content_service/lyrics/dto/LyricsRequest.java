package com.bit_rhythm.content_service.lyrics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Everything is optional: send only what the artist has. credits replaces the whole list of credits of the track. */
public record LyricsRequest(
        @Size(max = 20000) String plainText,
        @Size(max = 100000) String syncedLrc,
        @Size(max = 50) List<@Valid CreditDto> credits
) {
}
