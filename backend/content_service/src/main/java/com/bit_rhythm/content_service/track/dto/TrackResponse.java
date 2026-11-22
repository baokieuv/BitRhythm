package com.bit_rhythm.content_service.track.dto;

import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.genre.Genre;
import com.bit_rhythm.content_service.track.AudioStatus;
import com.bit_rhythm.content_service.track.Track;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
        Instant publishedAt,
        boolean liked             // has the viewer liked it?
) {
    public TrackResponse(ResultSet rs, String coverUrl) throws SQLException {
        this(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getLong("artist_id"),
                rs.getString("artist_name"),
                rs.getLong("album_id"),
                rs.getString("album_title"),
                rs.getInt("track_number"),
                rs.getString("genre"),
                coverUrl,
                rs.getInt("duration_seconds"),
                rs.getBoolean("explicit_content"),
                rs.getLong("play_count"),
                rs.getLong("like_count"),
                ContentStatus.valueOf(rs.getString("status")),
                AudioStatus.valueOf(rs.getString("audio_status")),
                rs.getTimestamp("published_at").toInstant(),
                false
        );
    }
}
