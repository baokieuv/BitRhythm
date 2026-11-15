package com.bit_rhythm.content_service.event.outbox;

import com.bit_rhythm.core.outbox.OutboxEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Same shape as auth-service's AuthOutboxEntity (and the same core OutboxEvent contract). */
@Entity
@Table(name = "outbox")
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ContentOutboxEntity implements OutboxEvent {

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
    private Integer isProcessed = 0;   // 0 = not published yet, 1 = published

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override public Long getEventId() { return eventId; }
    @Override public String getAggregateType() { return aggregateType; }
    @Override public String getAggregateId() { return aggregateId; }
    @Override public String getEventType() { return eventType; }
    @Override public String getPayload() { return payload; }
    @Override public Integer isProcessed() { return isProcessed; }
    @Override public LocalDateTime getCreatedAt() { return createdAt; }
}
