package com.bit_rhythm.auth_service.common.util;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** Helpers for space-separated OAuth2 strings (scope, audience). */
public final class ScopeUtils {

    private ScopeUtils() {
    }

    public static List<String> parse(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.trim().split("\\s+")).distinct().toList();
    }

    public static String join(Collection<String> values) {
        return String.join(" ", values);
    }
}
