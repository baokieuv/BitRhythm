package com.bit_rhythm.notification_service.inbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InboxRepository extends JpaRepository<InboxNotification, Long> {

    // Keyset (cursor) pagination: stable while new notifications arrive, cheap on big inboxes.
    List<InboxNotification> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursor, Pageable pageable);

    List<InboxNotification> findByUserIdAndReadAtIsNullAndIdLessThanOrderByIdDesc(
            Long userId, Long cursor, Pageable pageable);

    long countByUserIdAndReadAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE InboxNotification n SET n.readAt = :now WHERE n.id = :id AND n.userId = :userId AND n.readAt IS NULL")
    int markRead(@Param("id") Long id, @Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE InboxNotification n SET n.readAt = :now WHERE n.userId = :userId AND n.readAt IS NULL")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM InboxNotification n WHERE n.id = :id AND n.userId = :userId")
    int deleteOwned(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM InboxNotification n WHERE n.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
