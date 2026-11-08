package com.bit_rhythm.notification_service.template;

import com.bit_rhythm.core.entity.BaseEntity;
import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.core.NotificationType;
import com.bit_rhythm.notification_service.template.dto.request.NotificationTemplateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

/** A template edited through the admin API. When none is active, the file in resources/templates is used. */
@Entity
@Table(name = "notification_templates")
@Getter
@Setter
@NoArgsConstructor
public class NotificationTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false)
    private int version;

    private String subject;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String body;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public NotificationTemplate(NotificationTemplateRequest request) {

    }

    public void update(NotificationTemplateRequest request) {

    }
}
