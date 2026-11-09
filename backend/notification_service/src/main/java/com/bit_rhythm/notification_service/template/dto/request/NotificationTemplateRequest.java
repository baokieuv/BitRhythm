package com.bit_rhythm.notification_service.template.dto.request;

import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.core.NotificationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Create / update a template stored in the database. Placeholders in subject and body are written {{name}}
 * and filled from the event data.
 *
 * type + channel + locale + version identify a template. When creating, version is optional (1 if absent).
 * When updating, those four must match the stored template (they cannot be changed: create a new version instead);
 * only subject, body and active change.
 */
public record NotificationTemplateRequest(

        @NotNull
        NotificationType type,

        @NotNull
        Channel channel,

        /** "vi", "en", also accepted as "vi-VN": only the language part is stored. */
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{2,3}([-_][A-Za-z0-9]{2,8})*$", message = "must look like 'vi' or 'en-US'")
        String locale,

        @Min(1)
        Integer version,

        /** EMAIL: the subject line. IN_APP: the inbox title. May be empty: the default message is used. */
        @Size(max = 255)
        String subject,

        /** EMAIL: HTML. IN_APP: plain text. */
        @NotBlank
        String body,

        /** true by default when creating; when updating, null leaves the current value. */
        Boolean active
) {
}