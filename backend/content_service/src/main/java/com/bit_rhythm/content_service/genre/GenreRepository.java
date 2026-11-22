package com.bit_rhythm.content_service.genre;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    boolean existsByName(String name);

    boolean existsById(@NonNull Long id);

    List<Genre> findByIdIn(Collection<Long> ids);   // names of the genres of a page of tracks, in ONE query

    List<Genre> findAllOrderByNameAsc();
}
