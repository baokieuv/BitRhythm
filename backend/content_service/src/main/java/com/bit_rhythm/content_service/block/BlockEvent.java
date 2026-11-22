package com.bit_rhythm.content_service.block;

public record BlockEvent(
        String eventId,
        String eventType,
        Long blockerId,
        Long blockedId,
        boolean isBlocked
) {
}
