package com.bit_rhythm.content_service.lifecycle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Account event published by auth-service on the topic content.topics.auth-events (JSON, flat). Same contract as in user-service:
 *
 * <pre>
 * { "eventId": "user-deleted:42", "eventType": "USER_DELETED", "version": 1, "occurredAt": "...", "userId": 42 }
 * </pre>
 *
 * This service reacts to USER_DISABLED and USER_DELETED only (everything else is ignored). Unknown fields are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthEvent(
        String eventId,
        String eventType,
        Long userId,
        String email,
        String username,
        Instant occurredAt
) {
}
