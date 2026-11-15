package com.bit_rhythm.content_service.genre;

import com.bit_rhythm.content_service.genre.dto.GenreRequest;
import com.bit_rhythm.content_service.genre.dto.GenreResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {

    /** [S1] TODO(hint): GenreRepository.findAllByOrderByNameAsc, mapped to GenreResponse. A tiny table: cache it (Redis or in memory) once it is measured to matter. */
    public List<GenreResponse> list() {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): admin. slug = lower-case name with spaces and symbols turned into "-"; a duplicate name or slug -> 409 (add GENRE_ALREADY_EXISTS to ContentErrorCode). */
    public GenreResponse create(GenreRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): admin. GENRE_NOT_FOUND when missing. Keep the slug stable (URLs and clients use it): rename the name only. */
    public GenreResponse rename(Long id, GenreRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
