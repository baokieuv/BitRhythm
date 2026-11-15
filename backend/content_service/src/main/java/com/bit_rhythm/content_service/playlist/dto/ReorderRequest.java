package com.bit_rhythm.content_service.playlist.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** The track ids in their NEW order: exactly the tracks of the playlist, each once. */
public record ReorderRequest(@NotEmpty List<Long> trackIds) {
}
