package com.bit_rhythm.content_service.artist;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByOwnerUserId(Long ownerUserId);

    List<Artist> findByIdIn(Collection<Long> ids);   // artist names of a page of tracks, in ONE query

    Optional<Artist> findByOwnerUserIdAndIsDeleted(Long ownerUserId, Integer isDeleted);

    /** Prefix search (uses idx_artist_name). The escape character is '!': escape %, _ and ! in the prefix before calling (see UserSearchService of user-service). */
    @Query("SELECT a FROM Artist a WHERE a.name LIKE CONCAT(:prefix, '%') ESCAPE '!' AND a.id < :cursor ORDER BY a.id DESC")
    List<Artist> searchByNamePrefix(@Param("prefix") String prefix, @Param("cursor") Long cursor, Pageable pageable);
}
