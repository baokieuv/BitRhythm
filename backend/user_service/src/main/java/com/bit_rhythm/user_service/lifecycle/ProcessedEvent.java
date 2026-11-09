package com.bit_rhythm.user_service.lifecycle;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** [S2] chống xử lý trùng event từ auth-service. */
@Entity
@Table(name = "processed_events")
@Getter @NoArgsConstructor
public class ProcessedEvent {
    @Id
    private String eventId;
    private LocalDateTime processedAt = LocalDateTime.now();

    public ProcessedEvent(String eventId) { this.eventId = eventId; }
}
