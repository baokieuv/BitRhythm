package com.bit_rhythm.notification_service.core;

import lombok.Getter;

/** Decides whether a user may opt out: TRANSACTIONAL and SECURITY are always delivered. */
@Getter
public enum NotificationCategory {
    TRANSACTIONAL(true),
    SECURITY(true),
    SOCIAL(false),
    MARKETING(false);

    private final boolean mandatory;

    NotificationCategory(boolean mandatory) {
        this.mandatory = mandatory;
    }
}
