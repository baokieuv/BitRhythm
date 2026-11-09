package com.bit_rhythm.auth_service.event;

import java.time.Instant;
import java.util.Map;

/**
 * Message published to the notification topic and consumed by notification-service. This is its wire contract
 * (JSON): keep it identical to notification_service.event.NotificationEvent, only ADD fields, and bump "version"
 * for a breaking change.
 *
 * <pre>
 * { "eventId": "uuid", "eventType": "EMAIL_VERIFICATION_REQUESTED", "version": 1,
 *   "occurredAt": "2026-11-08T10:15:30Z",
 *   "recipient": { "userId": 42, "email": "a@b.com", "locale": null },
 *   "data": { "verifyUrl": "https://...", "username": "ann" },
 *   "metadata": { "source": "auth-service", "correlationId": "..." } }
 * </pre>
 *
 * eventType is the NAME of notification-service's NotificationType. It is a String here on purpose: auth-service
 * does not depend on that enum. It contains no password or token hash; the one-time link is in data.
 */
public record NotificationEvent(
        String eventId,
        String eventType,
        int version,
        Instant occurredAt,
        Recipient recipient,
        Map<String, Object> data,
        Metadata metadata) {

    /** Contact data travels with the event, so notification-service never has to call auth-service back. */
    public record Recipient(Long userId, String email, String locale) {
    }

    public record Metadata(String source, String correlationId) {
    }
}