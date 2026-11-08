package com.bit_rhythm.notification_service.channel.email;

public record EmailMessage(String to, String subject, String htmlBody) {
}
