package com.bit_rhythm.content_service.album;

import com.bit_rhythm.content_service.common.ContentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByIdIn(Collection<Long> ids);

    // Keyset pagination (cursor = id of the last row received, Long.MAX_VALUE for the first page). Ask for size + 1 rows.
    List<Album> findByStatusAndIdLessThanOrderByIdDesc(ContentStatus status, Long cursor, Pageable pageable);          // new albums

    List<Album> findByArtistIdAndStatusAndIdLessThanOrderByIdDesc(Long artistId, ContentStatus status, Long cursor, Pageable pageable);   // artist page

    List<Album> findByArtistIdAndIdLessThanOrderByIdDesc(Long artistId, Long cursor, Pageable pageable);               // studio: all statuses

    /** Prefix search on published albums (uses idx_album_title). Escape character '!': escape %, _ and ! in the prefix first. */
    @Query("SELECT a FROM Album a WHERE a.title LIKE CONCAT(:prefix, '%') ESCAPE '!' AND a.status = com.bit_rhythm.content_service.common.ContentStatus.PUBLISHED AND a.id < :cursor ORDER BY a.id DESC")
    List<Album> searchPublishedByTitlePrefix(@Param("prefix") String prefix, @Param("cursor") Long cursor, Pageable pageable);

    List<Album> findByArtistId(Long artistId);   // [S2] account deleted: hide everything of the artist
}
