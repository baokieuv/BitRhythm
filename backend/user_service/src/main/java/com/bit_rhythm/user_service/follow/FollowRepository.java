package com.bit_rhythm.user_service.follow;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    // Keyset pagination (cursor = id of the last row received, Long.MAX_VALUE for the first page). Ask for size + 1 rows.
    List<Follow> findByFolloweeIdAndIdLessThanOrderByIdDesc(Long followeeId, Long cursor, Pageable pageable);   // followers

    List<Follow> findByFollowerIdAndIdLessThanOrderByIdDesc(Long followerId, Long cursor, Pageable pageable);   // following

    /** Returns how many rows were deleted (0 or 1): adjust the counters ONLY when it is 1. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Follow f WHERE f.followerId = :follower AND f.followeeId = :followee")
    int deletePair(@Param("follower") Long follower, @Param("followee") Long followee);

    // [S2] block: remove the follow in both directions
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Follow f WHERE (f.followerId = :a AND f.followeeId = :b) OR (f.followerId = :b AND f.followeeId = :a)")
    int deleteBetween(@Param("a") Long a, @Param("b") Long b);

    // [S2] account deleted: who has to have a counter corrected before the rows are removed
    List<Follow> findByFollowerId(Long followerId);

    List<Follow> findByFolloweeId(Long followeeId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Follow f WHERE f.followerId = :user OR f.followeeId = :user")
    int deleteAllOf(@Param("user") Long user);
}
