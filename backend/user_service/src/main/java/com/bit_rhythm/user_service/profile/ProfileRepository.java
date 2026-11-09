package com.bit_rhythm.user_service.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<UserProfile, Long> {

    @Modifying
    @Query("update UserProfile p set p.followerCount = p.followerCount + :delta where p.userId = :id")
    int adjustFollowerCount(@Param("id") Long id, @Param("delta") long delta);

    @Modifying
    @Query("update UserProfile p set p.followingCount = p.followingCount + :delta where p.userId = :id")
    int adjustFollowingCount(@Param("id") Long id, @Param("delta") long delta);
}
