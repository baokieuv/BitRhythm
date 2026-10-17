package com.bit_rhythm.core.dto;

import com.bit_rhythm.core.common.NotificationType;

public record NotificationEventDto (
        String keycloakId,
        String title,
        String message,
        NotificationType type // Hoặc dùng thẳng Enum NotificationType nếu bạn đã tạo
) {

}
