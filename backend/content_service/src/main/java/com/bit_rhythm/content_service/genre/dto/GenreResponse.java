package com.bit_rhythm.content_service.genre.dto;

import com.bit_rhythm.content_service.genre.Genre;

public record GenreResponse(Long id, String name, String slug) {

    public static GenreResponse from(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName(), genre.getSlug());
    }
}
