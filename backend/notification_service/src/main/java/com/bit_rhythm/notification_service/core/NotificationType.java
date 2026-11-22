package com.bit_rhythm.notification_service.core;

import com.bit_rhythm.notification_service.channel.Channel;

import java.util.List;

/**
 * Every notification the system can send. The constant name is what other services put in the event
 * ("eventType"). templateKey names the email template file and the i18n keys.
 */
public enum NotificationType {

    EMAIL_VERIFICATION_REQUESTED("email_verification", NotificationCategory.TRANSACTIONAL, Channel.EMAIL),
    PASSWORD_RESET_REQUESTED("password_reset", NotificationCategory.TRANSACTIONAL, Channel.EMAIL),
    PASSWORD_CHANGED("password_changed", NotificationCategory.SECURITY, Channel.EMAIL, Channel.IN_APP),
    NEW_FOLLOWER("new_follower", NotificationCategory.SOCIAL, Channel.IN_APP),
    ARTIST_VERIFICATION_RESULT("artist_verification_result", NotificationCategory.TRANSACTIONAL, Channel.EMAIL, Channel.IN_APP),
    TRACK_PUBLISHED("track_published", NotificationCategory.SOCIAL, Channel.IN_APP),
    SYSTEM_ANNOUNCEMENT("system_announcement", NotificationCategory.MARKETING, Channel.IN_APP);

    private final String templateKey;
    private final NotificationCategory category;
    private final List<Channel> defaultChannels;

    NotificationType(String templateKey, NotificationCategory category, Channel... defaultChannels) {
        this.templateKey = templateKey;
        this.category = category;
        this.defaultChannels = List.of(defaultChannels);
    }

    public String templateKey() {
        return templateKey;
    }

    public NotificationCategory category() {
        return category;
    }

    public List<Channel> defaultChannels() {
        return defaultChannels;
    }
}
