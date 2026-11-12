package com.bit_rhythm.user_service.lifecycle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Account event published by auth-service on the topic user.topics.auth-events (JSON, flat). Contract:
 *
 * <pre>
 * { "eventId": "user-registered:42", "eventType": "USER_REGISTERED", "version": 1,
 *   "occurredAt": "2026-11-08T10:15:30Z",
 *   "userId": 42, "email": "ann@example.com", "username": "ann" }
 * </pre>
 *
 * eventType: USER_REGISTERED | USER_DISABLED | USER_DELETED (others are ignored). email and username are only needed
 * by USER_REGISTERED. eventId must be stable per fact (e.g. "user-registered:{userId}"): it is the idempotency key.
 * Unknown fields are ignored so auth-service can add fields without breaking this consumer.
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