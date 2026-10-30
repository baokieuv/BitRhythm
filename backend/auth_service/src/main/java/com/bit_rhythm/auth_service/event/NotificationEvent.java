package com.bit_rhythm.auth_service.event;

import java.time.Instant;

/**
 * Command consumed by notification_service. It contains no password or token hash.
 */
public record NotificationEvent(
        String eventId,
        String eventType,
        String recipientEmail,
        String recipientName,
        String subject,
        String actionUrl,
        Instant occurredAt) {
}
