package com.bit_rhythm.user_service.event.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** TODO: implements OutboxEvent của core nếu dùng chung. */
@Entity
@Table(name = "outbox")
@Getter @NoArgsConstructor
public class UserOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;     // dùng làm Kafka key
    private String eventId;
    private String eventType;
    private String topic;

    @Column(columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime sentAt;

    public UserOutboxEntity(String aggregateId, String eventId, String eventType, String topic, String payload) {
        this.aggregateId = aggregateId;
        this.eventId = eventId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
}
