package com.bit_rhythm.content_service.history;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    // Keyset pagination (cursor = id of the last row received, Long.MAX_VALUE for the first page). Ask for size + 1 rows.
    List<PlayHistory> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);

    /** Retention: returns how many rows were deleted. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlayHistory h WHERE h.playedAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlayHistory h WHERE h.userId = :userId")
    int deleteAllOfUser(@Param("userId") Long userId);   // [S2] account deleted
}
