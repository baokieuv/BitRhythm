package com.bit_rhythm.user_service.lifecycle;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** [S2] An event of auth-service that was already handled: the primary key makes the consumer idempotent. */
@Entity
@Table(name = "processed_events")
@Getter
@NoArgsConstructor
public class ProcessedEvent extends BaseEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessedEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}
