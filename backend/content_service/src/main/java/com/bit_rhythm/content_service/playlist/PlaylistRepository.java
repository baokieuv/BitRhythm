package com.bit_rhythm.content_service.playlist;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // Keyset pagination (cursor = id of the last row received, Long.MAX_VALUE for the first page). Ask for size + 1 rows.
    List<Playlist> findByOwnerUserIdAndIdLessThanOrderByIdDesc(Long ownerUserId, Long cursor, Pageable pageable);

    /** Atomic counter; delta is +1 / -1 (or the number of rows removed). Same transaction as the insert / delete in playlist_tracks. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Playlist p SET p.trackCount = p.trackCount + :delta WHERE p.id = :id")
    int addTracks(@Param("id") Long id, @Param("delta") int delta);

    List<Playlist> findByOwnerUserId(Long ownerUserId);   // [S2] account deleted

    int countByOwnerUserId(Long ownerUserId);

    Optional<Playlist> findByIdAndIsDeleted(Long id, Integer isDeleted);

}
