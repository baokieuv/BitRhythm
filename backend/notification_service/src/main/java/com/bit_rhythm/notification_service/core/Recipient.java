package com.bit_rhythm.notification_service.core;

import jakarta.validation.constraints.NotNull;

/** Who receives the notification. Events carry the contact data so this service never calls auth-service back. */
public record Recipient(
        @NotNull Long userId,
        String email,
        String locale   // "vi", "en"; falls back to English
) {
}
