package com.bit_rhythm.auth_service.event;

import com.bit_rhythm.core.outbox.OutboxEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "auth_service", name = "outbox")
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class AuthOutboxEntity implements OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long eventId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "is_processed", nullable = false)
    private Integer isProcessed = 0; // 0 = not processed, 1 = processed

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters overriding the OutboxEvent interface
    @Override public Long getEventId() { return eventId; }
    @Override public String getAggregateType() { return aggregateType; }
    @Override public String getAggregateId() { return aggregateId; }
    @Override public String getEventType() { return eventType; }
    @Override public String getPayload() { return payload; }
    @Override public Integer isProcessed() { return isProcessed; }
    @Override public LocalDateTime getCreatedAt() { return createdAt; }
}