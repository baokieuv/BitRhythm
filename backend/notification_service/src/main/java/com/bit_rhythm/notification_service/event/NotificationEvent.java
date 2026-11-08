package com.bit_rhythm.notification_service.event;

import com.bit_rhythm.notification_service.core.NotificationType;
import com.bit_rhythm.notification_service.core.Recipient;

import java.time.Instant;
import java.util.Map;

/**
 * Wire contract of the notification.events topic (JSON). Keep it backward compatible: only ADD fields,
 * bump "version" for breaking changes.
 *
 * <pre>
 * { "eventId": "uuid", "eventType": "EMAIL_VERIFICATION_REQUESTED", "version": 1,
 *   "occurredAt": "2026-11-08T10:15:30Z",
 *   "recipient": { "userId": 42, "email": "a@b.com", "locale": "vi" },
 *   "data": { "verifyUrl": "https://..." },
 *   "metadata": { "source": "auth-service", "correlationId": "..." } }
 * </pre>
 */
public record NotificationEvent(
        String eventId,
        NotificationType eventType,
        int version,
        Instant occurredAt,
        Recipient recipient,
        Map<String, Object> data,
        Metadata metadata
) {
    public record Metadata(String source, String correlationId) {
    }
}
