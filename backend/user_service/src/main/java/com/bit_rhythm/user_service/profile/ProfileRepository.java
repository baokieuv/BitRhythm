package com.bit_rhythm.user_service.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface ProfileRepository extends JpaRepository<UserProfile, Long> {

    List<UserProfile> findByUserIdIn(Collection<Long> ids);   // summaries, follower lists

    /**
     * Atomic counter updates: "SET x = x + :delta" runs inside the database, so two concurrent follows cannot
     * lose an update. delta is +1 or -1. Call them in the same transaction as the insert/delete in follows.
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserProfile p SET p.followerCount = p.followerCount + :delta WHERE p.userId = :id")
    int addFollowers(@Param("id") Long id, @Param("delta") long delta);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserProfile p SET p.followingCount = p.followingCount + :delta WHERE p.userId = :id")
    int addFollowing(@Param("id") Long id, @Param("delta") long delta);

    // [S2] search by name. TODO(hint): prefix match uses idx_profile_name; add the cursor condition (user_id > :cursor)
    //      and exclude blocked users in the service. status is ACTIVE for normal searches.
    List<UserProfile> findByDisplayNameStartingWithIgnoreCaseAndStatusOrderByUserIdAsc(
            String prefix, ProfileStatus status, org.springframework.data.domain.Pageable pageable);

    // [S2] admin listing
    org.springframework.data.domain.Page<UserProfile> findByStatus(
            ProfileStatus status, org.springframework.data.domain.Pageable pageable);
}
