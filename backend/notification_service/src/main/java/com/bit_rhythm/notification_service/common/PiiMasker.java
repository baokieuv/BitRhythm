package com.bit_rhythm.notification_service.common;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/** Everything that prints an address or a token (logs, notification_log) goes through here. */
public final class PiiMasker {
    private PiiMasker() {
    }

    /** john@example.com -> j***@example.com */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    /** Keeps only the last 4 characters. */
    public static String maskToken(String token) {
        if (token == null || token.length() <= 4) {
            return "***";
        }
        return "***" + token.substring(token.length() - 4);
    }

}
