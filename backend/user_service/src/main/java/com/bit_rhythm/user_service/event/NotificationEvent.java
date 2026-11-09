package com.bit_rhythm.user_service.event;

import java.time.Instant;
import java.util.Map;

/** Hợp đồng của notification-service. Đối chiếu lại với class thật bên đó. */
public record NotificationEvent(String eventId, String eventType, Recipient recipient,
                                Map<String, Object> data, Map<String, Object> metadata, Instant occurredAt) {

    public record Recipient(Long userId, String email, String locale) {}
}
