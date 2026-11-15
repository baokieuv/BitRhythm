package com.bit_rhythm.content_service.track.dto;

import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.track.AudioStatus;

import java.time.LocalDateTime;

/** A track as shown in lists and detail pages. The audio key is never exposed: listening goes through GET /tracks/{id}/stream. */
public record TrackResponse(
        Long id,
        String title,
        Long artistId,
        String artistName,
        Long albumId,
        String albumTitle,
        Integer trackNumber,
        String genre,
        String coverUrl,
        Integer durationSeconds,
        boolean explicitContent,
        long playCount,
        long likeCount,
        ContentStatus status,
        AudioStatus audioStatus,
        LocalDateTime publishedAt,
        boolean liked             // has the viewer liked it?
) {
}
