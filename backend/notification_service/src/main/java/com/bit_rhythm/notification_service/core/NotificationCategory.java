package com.bit_rhythm.notification_service.core;

/** Decides whether a user may opt out: TRANSACTIONAL and SECURITY are always delivered. */
public enum NotificationCategory {
    TRANSACTIONAL(true),
    SECURITY(true),
    SOCIAL(false),
    MARKETING(false);

    private final boolean mandatory;

    NotificationCategory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean mandatory() {
        return mandatory;
    }
}
