package com.bit_rhythm.notification_service.channel.email;

/** Adapter for an email backend (SMTP, SES, SendGrid...). Swapping providers must not touch business code. */
public interface EmailProvider {

    String name();

    /** @return the provider's message id, stored in notification_log.provider_message_id */
    String send(EmailMessage message);
}