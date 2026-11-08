package com.bit_rhythm.notification_service.internal;

import com.bit_rhythm.notification_service.core.NotificationType;
import com.bit_rhythm.notification_service.core.Recipient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record InternalNotificationRequest(
        @NotBlank String idempotencyKey,   // the caller retries safely: same key = delivered once
        @NotNull NotificationType type,
        @NotNull @Valid Recipient recipient,
        Map<String, Object> data
) {
}
