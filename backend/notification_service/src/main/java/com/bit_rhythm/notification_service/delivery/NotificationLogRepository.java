package com.bit_rhythm.notification_service.delivery;

import com.bit_rhythm.notification_service.channel.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Optional<NotificationLog> findByEventIdAndChannel(String eventId, Channel channel);

    Page<NotificationLog> findByStatus(DeliveryStatus status, Pageable pageable);

    Page<NotificationLog> findByUserId(Long userId, Pageable pageable);

    Page<NotificationLog> findByUserIdAndStatus(Long userId, DeliveryStatus status, Pageable pageable);

    /** Atomic "take it": only one of two concurrent resends gets 1 and may go on. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationLog l SET l.status = :to WHERE l.id = :id AND l.status = :from")
    int changeStatus(@Param("id") Long id, @Param("from") DeliveryStatus from, @Param("to") DeliveryStatus to);

    @Modifying
    @Query("DELETE FROM NotificationLog l WHERE l.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
