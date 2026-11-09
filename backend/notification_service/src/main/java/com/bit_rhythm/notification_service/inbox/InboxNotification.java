package com.bit_rhythm.notification_service.inbox;

import com.bit_rhythm.core.entity.BaseEntity;
import com.bit_rhythm.notification_service.core.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_notifications")
@Getter
@Setter
@NoArgsConstructor
public class InboxNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    private String body;

    /** Extra payload for the client (deep link, ids...) */
    @Column(name = "data_json", columnDefinition = "longtext")
    private String dataJson;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
