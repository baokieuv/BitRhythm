package com.bit_rhythm.content_service.playlist.dto;

import com.bit_rhythm.content_service.playlist.Playlist;
import com.bit_rhythm.content_service.playlist.PlaylistVisibility;

import java.time.Instant;

public record PlaylistResponse(
        Long id,
        Long ownerUserId,
        String name,
        String description,
        PlaylistVisibility visibility,
        int trackCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(playlist.getId(), playlist.getOwnerUserId(), playlist.getName(), playlist.getDescription(),
                playlist.getVisibility(), playlist.getTrackCount(), playlist.getCreatedAt(), playlist.getUpdatedAt());
    }
}
