package com.bit_rhythm.content_service.block;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /** True when either user blocked the other: the check used by profile and follow. */
    @Query("SELECT COUNT(b) > 0 FROM UserBlock b WHERE (b.blockerId = :a AND b.blockedId = :b) OR (b.blockerId = :b AND b.blockedId = :a)")
    boolean existsEitherWay(@Param("a") Long a, @Param("b") Long b);

    /** Among these users, the ones the viewer blocked. One query for a whole page of search results. */
    @Query("SELECT b.blockedId FROM UserBlock b WHERE b.blockerId = :viewer AND b.blockedId IN :ids")
    List<Long> findBlockedIdsAmong(@Param("viewer") Long viewer, @Param("ids") Collection<Long> ids);

    /** Among these users, the ones who blocked the viewer. */
    @Query("SELECT b.blockerId FROM UserBlock b WHERE b.blockedId = :viewer AND b.blockerId IN :ids")
    List<Long> findBlockerIdsAmong(@Param("viewer") Long viewer, @Param("ids") Collection<Long> ids);

    List<UserBlock> findByBlockerIdAndIdLessThanOrderByIdDesc(Long blockerId, Long cursor, Pageable pageable);

    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}