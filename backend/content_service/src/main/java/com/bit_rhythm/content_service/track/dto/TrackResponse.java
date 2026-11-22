package com.bit_rhythm.content_service.track.dto;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.genre.Genre;
import com.bit_rhythm.content_service.track.AudioStatus;
import com.bit_rhythm.content_service.track.Track;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import static com.bit_rhythm.content_service.common.utils.ResultSetUtils.*;

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
                getLong(rs, "id", null),
                getString(rs, "title", null),
                getLong(rs, "artist_id", null),
                getString(rs, "artist_name", null),
                getLong(rs, "album_id", null),
                getString(rs, "album_title", null),
                getInt(rs, "track_number", null),
                getString(rs, "genre", null),
                coverUrl,
                getInt(rs, "duration_seconds", null),
                getBoolean(rs, "explicit_content", false),
                getLong(rs, "play_count", 0L),
                getLong(rs, "like_count", 0L),
                getEnum(rs, "status", ContentStatus.class, null),
                getEnum(rs, "audio_status", AudioStatus.class, null),
                getInstant(rs, "published_at"),
                getBoolean(rs, "liked", false)
        );
    }

    public static TrackResponse from(Track track, String coverUrl, Artist artist, Album album, Genre genre, boolean liked) {
        return new TrackResponse(
                track.getId(),
                track.getTitle(),
                track.getArtistId(),
                artist.getName(),
                track.getAlbumId(),
                album.getTitle(),
                track.getTrackNumber(),
                genre.getName(),
                coverUrl,
                track.getDurationSeconds(),
                track.isExplicitContent(),
                track.getPlayCount(),
                track.getLikeCount(),
                track.getStatus(),
                track.getAudioStatus(),
                track.getPublishedAt(),
                liked
        );
    }
}
