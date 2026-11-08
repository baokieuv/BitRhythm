package com.bit_rhythm.notification_service.delivery;

public enum DeliveryStatus {
    PENDING,   // row created, send not finished (or failed and waiting for a retry)
    SENT,
    FAILED,    // last attempt failed
    SKIPPED,   // suppressed, rate limited, ...
    BOUNCED    // set by the provider webhook
}
