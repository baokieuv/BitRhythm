package com.bit_rhythm.content_service.album.dto;

import com.bit_rhythm.content_service.common.ContentStatus;

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
        LocalDateTime publishedAt,
        boolean saved             // has the viewer saved it to their library?
) {
}
