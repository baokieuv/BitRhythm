package com.bit_rhythm.content_service.genre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    List<Genre> findByIdIn(Collection<Long> ids);   // names of the genres of a page of tracks, in ONE query

    List<Genre> findAllByOrderByNameAsc();
}
