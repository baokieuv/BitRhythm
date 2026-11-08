package com.bit_rhythm.notification_service.template.dto.response;

import com.bit_rhythm.notification_service.template.NotificationTemplate;

public record NotificationTemplateResponse(

) {
    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return new NotificationTemplateResponse(

        );
    }
}
