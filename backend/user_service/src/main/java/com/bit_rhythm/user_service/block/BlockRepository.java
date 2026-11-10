package com.bit_rhythm.user_service.block;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /** True when either user blocked the other: the check used by profile, follow and search. */
    @Query("SELECT COUNT(b) > 0 FROM UserBlock b WHERE (b.blockerId = :a AND b.blockedId = :b) OR (b.blockerId = :b AND b.blockedId = :a)")
    boolean existsEitherWay(@Param("a") Long a, @Param("b") Long b);

    List<UserBlock> findByBlockerIdAndIdLessThanOrderByIdDesc(Long blockerId, Long cursor, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserBlock b WHERE b.blockerId = :blocker AND b.blockedId = :blocked")
    int deletePair(@Param("blocker") Long blocker, @Param("blocked") Long blocked);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserBlock b WHERE b.blockerId = :user OR b.blockedId = :user")
    int deleteAllOf(@Param("user") Long user);
}
