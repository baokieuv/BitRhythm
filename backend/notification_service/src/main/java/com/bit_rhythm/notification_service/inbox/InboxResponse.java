package com.bit_rhythm.notification_service.inbox;

import com.bit_rhythm.notification_service.core.NotificationType;

import java.time.LocalDateTime;

public record InboxResponse(
        Long id,
        NotificationType type,
        String title,
        String body,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static InboxResponse from(InboxNotification n) {
        return new InboxResponse(n.getId(), n.getType(), n.getTitle(), n.getBody(), n.getReadAt(), n.getCreatedAt());
    }
}
