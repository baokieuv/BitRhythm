package com.bit_rhythm.content_service.genre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findBySlugAndIsDeleted(String slug, Integer isDeleted);

    Optional<Genre> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByNameAndIsDeleted(String name, Integer isDeleted);

    boolean existsBySlugAndIsDeleted(String slug, Integer isDeleted);

    boolean existsByIdAndIsDeleted(Long id, Integer isDeleted);

    List<Genre> findByIdInAndIsDeleted(Collection<Long> ids, Integer isDeleted);   // names of the genres of a page of tracks, in ONE query

    List<Genre> findAllByIsDeletedOrderByNameAsc(Integer isDeleted);
}
