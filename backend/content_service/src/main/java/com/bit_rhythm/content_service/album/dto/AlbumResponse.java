package com.bit_rhythm.content_service.album.dto;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.common.ContentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AlbumResponse(
        Long id,
        Long artistId,
        String artistName,
        String title,
        String coverUrl,
        LocalDate releaseDate,
        ContentStatus status,
        Instant publishedAt,
        boolean saved             // has the viewer saved it to their library?
) {

    public AlbumResponse(Album album, Artist artist, String coverUrl) {
        this(
                album.getId(),
                album.getArtistId(),
                artist.getName(),
                album.getTitle(),
                coverUrl,
                album.getReleaseDate(),
                album.getStatus(),
                album.getPublishedAt(),
                false
        );
    }

    public AlbumResponse(Album album, Artist artist, String coverUrl, boolean saved) {
        this(
                album.getId(),
                album.getArtistId(),
                artist.getName(),
                album.getTitle(),
                coverUrl,
                album.getReleaseDate(),
                album.getStatus(),
                album.getPublishedAt(),
                saved
        );
    }
}
