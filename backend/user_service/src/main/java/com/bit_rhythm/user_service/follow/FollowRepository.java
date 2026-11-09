package com.bit_rhythm.user_service.follow;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    @Modifying
    @Query("delete from Follow f where f.followerId = :follower and f.followeeId = :followee")
    int deleteRelation(@Param("follower") Long follower, @Param("followee") Long followee);

    @Query("select f from Follow f where f.followeeId = :userId and f.id < :cursor order by f.id desc")
    List<Follow> findFollowers(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("select f from Follow f where f.followerId = :userId and f.id < :cursor order by f.id desc")
    List<Follow> findFollowing(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);
}
