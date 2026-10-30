package com.bit_rhythm.core.outbox;

import java.time.LocalDateTime;

public interface OutboxEvent {
    Long getEventId();
    String getAggregateType();
    String getAggregateId();
    String getEventType();
    String getPayload();
    Integer isProcessed();
    LocalDateTime getCreatedAt();
}
