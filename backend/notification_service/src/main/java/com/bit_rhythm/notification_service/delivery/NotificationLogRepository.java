package com.bit_rhythm.notification_service.delivery;

import com.bit_rhythm.notification_service.channel.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Optional<NotificationLog> findByEventIdAndChannel(String eventId, Channel channel);

    Page<NotificationLog> findByStatus(DeliveryStatus status, Pageable pageable);

    Page<NotificationLog> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM NotificationLog l WHERE l.createdAt < :threshold")
    int deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
