package com.bit_rhythm.content_service.history;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    // Keyset pagination (cursor = id of the last row received, Long.MAX_VALUE for the first page). Ask for size + 1 rows.
    List<PlayHistory> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);

    /**
     * Retention, ONE statement: returns how many rows were deleted. The threshold is an Instant like PlayHistory.playedAt
     * (it was a LocalDateTime, which does not match the field type). For a big table use deleteBatchOlderThan.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlayHistory h WHERE h.playedAt < :threshold")
    int deleteOlderThan(@Param("threshold") Instant threshold);

    /** Retention in batches: deletes at most :limit rows (MariaDB DELETE ... LIMIT), each call is its own short transaction. */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM play_history WHERE played_at < :threshold LIMIT :limit", nativeQuery = true)
    int deleteBatchOlderThan(@Param("threshold") Instant threshold, @Param("limit") int limit);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlayHistory h WHERE h.userId = :userId")
    int deleteAllOfUser(@Param("userId") Long userId);   // [S2] account deleted

    List<PlayHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    interface TrackPlayCount { Long getTrackId(); long getPlays(); }

    @Query("SELECT p.trackId AS trackId, COUNT(p) AS plays FROM PlayHistory p WHERE p.playedAt >= :since "
            + "GROUP BY p.trackId ORDER BY COUNT(p) DESC")
    List<TrackPlayCount> countPlaysSince(@Param("since") Instant since, Pageable pageable);
}