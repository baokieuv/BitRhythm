package com.bit_rhythm.content_service.library;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface TrackLikeRepository extends JpaRepository<TrackLike, Long> {

    boolean existsByUserIdAndTrackId(Long userId, Long trackId);

    /** Which of these tracks did the user like? ONE query for a whole page ("liked" flag of the lists). */
    List<TrackLike> findByUserIdAndTrackIdIn(Long userId, Collection<Long> trackIds);

    // Keyset pagination (cursor = id of the last row received, Long.MAX_VALUE for the first page). Ask for size + 1 rows.
    List<TrackLike> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);

    /** Number of rows deleted (0 or 1): correct track.likeCount ONLY when it is 1. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TrackLike l WHERE l.userId = :userId AND l.trackId = :trackId")
    int deletePair(@Param("userId") Long userId, @Param("trackId") Long trackId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TrackLike l WHERE l.trackId = :trackId")
    int deleteAllOfTrack(@Param("trackId") Long trackId);   // the track was deleted

    List<TrackLike> findByUserId(Long userId);   // [S2] account deleted: tracks whose likeCount must go down by one

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TrackLike l WHERE l.userId = :userId")
    int deleteAllOfUser(@Param("userId") Long userId);
}
