package com.bit_rhythm.notification_service.core;

import com.bit_rhythm.notification_service.event.NotificationEvent;

import java.util.Map;

/** What the core pipeline works with, whether it came from Kafka or from the internal API. */
public record NotificationRequest(
        String eventId,          // idempotency key
        NotificationType type,
        Recipient recipient,
        Map<String, Object> data,
        String source
) {
    public static NotificationRequest from(NotificationEvent event) {
        return new NotificationRequest(
                event.eventId(),
                event.eventType(),
                event.recipient(),
                event.data() == null ? Map.of() : event.data(),
                event.metadata() == null ? null : event.metadata().source());
    }
}
