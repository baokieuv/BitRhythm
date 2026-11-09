package com.bit_rhythm.notification_service.template.dto.response;

import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.core.NotificationType;
import com.bit_rhythm.notification_service.template.NotificationTemplate;

import java.time.Instant;

public record NotificationTemplateResponse(
        Long id,
        NotificationType type,
        Channel channel,
        String locale,
        int version,
        String subject,
        String body,
        boolean active,
        Instant updatedAt
) {
    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getId(),
                template.getType(),
                template.getChannel(),
                template.getLocale(),
                template.getVersion(),
                template.getSubject(),
                template.getBody(),
                template.isActive(),
                template.getUpdatedAt());   // inherited from BaseEntity: remove this line if BaseEntity has no getter
    }
}