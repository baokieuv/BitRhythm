package com.bit_rhythm.notification_service.event;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationEvent(
        String eventId,                  // UUID, dùng cho idempotency
        NotificationEventType eventType, // EMAIL_VERIFICATION_REQUESTED, PASSWORD_RESET_REQUESTED, ...
        int version,
        LocalDateTime occurredAt,
        Recipient recipient,             // userId, email, locale
        Map<String, Object> data,        // verifyUrl / resetUrl / tên / nội dung inbox...
        Metadata metadata) {             // source, correlationId
    public record Recipient(Long userId, String email, String locale) {}
    public record Metadata(String source, String correlationId) {}
}
