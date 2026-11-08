package com.bit_rhythm.notification_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        Kafka kafka,
        Email email,
        Inbox inbox,
        RateLimit rateLimit,
        Retention retention,
        Unsubscribe unsubscribe
) {
    public record Kafka(String topic, String dltTopic, int maxRetries, Duration initialBackoff, Duration maxBackoff) {
    }

    /** provider: "smtp" (any SMTP server, spring.mail.*) or "gmail" (see GmailEmailProvider). */
    public record Email(String provider, String from, String fromName, Gmail gmail) {
    }

    /** appPassword is a Google App Password (16 characters), never the account password. */
    public record Gmail(String username, String appPassword, int dailyLimit) {
    }

    public record Inbox(Duration heartbeat, int maxPageSize) {
    }

    public record RateLimit(boolean enabled, int maxPerUserPerType, Duration window) {
    }

    public record Retention(Duration log, Duration inbox) {
    }

    public record Unsubscribe(String secret, String baseUrl) {
    }
}
