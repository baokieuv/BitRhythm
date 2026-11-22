package com.bit_rhythm.user_service.block;

import com.bit_rhythm.user_service.event.NotificationEvent;

public record BlockEvent (
        String eventId,
        String eventType,
        int version,
        Long blockerId,
        Long blockedId,
        boolean isBlocked,
        Metadata metadata
) {
    public record Metadata(String source, String correlationId) {
    }
}
