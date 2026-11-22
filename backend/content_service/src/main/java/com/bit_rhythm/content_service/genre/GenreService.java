package com.bit_rhythm.content_service.genre;

import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.genre.dto.GenreRequest;
import com.bit_rhythm.content_service.genre.dto.GenreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    /** List all non-deleted genres, ordered by name. */
    public List<GenreResponse> list() {

        List<Genre> genres = genreRepository.findAllOrderByNameAsc();
        return genres.stream()
                .map(GenreResponse::from)
                .toList();
    }

    /** Create a new genre. */
    public GenreResponse create(GenreRequest request) {

        if (genreRepository.existsByName(request.name())) {
            throw new ContentException(ContentErrorCode.GENRE_ALREADY_EXISTS);
        }

        String slug = request.name().toLowerCase().replaceAll("[^a-z0-9]+", "-");

        Genre genre = new Genre(request.name(), slug);

        return GenreResponse.from(genreRepository.save(genre));
    }

    /** Rename a genre by ID. */
    public GenreResponse rename(Long id, GenreRequest request) {

        if (!genreRepository.existsById(id)) {
            throw new ContentException(ContentErrorCode.GENRE_NOT_FOUND);
        }

        if (genreRepository.existsByName(request.name())) {
            throw new ContentException(ContentErrorCode.GENRE_ALREADY_EXISTS);
        }

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ContentException(ContentErrorCode.GENRE_NOT_FOUND));

        genre.setName(request.name());

        return GenreResponse.from(genreRepository.save(genre));
    }
}
