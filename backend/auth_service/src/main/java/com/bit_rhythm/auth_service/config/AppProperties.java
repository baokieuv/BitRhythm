package com.bit_rhythm.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Duration verificationTokenTtl,
        String verifyEmailUrl,
        String resetPasswordUrl,
        String notificationTopic,
        Admin admin,
        RateLimit rateLimit
) {

    public record Admin(String email, String username, String password) {
    }

    public record RateLimit(boolean enabled, List<Rule> rules) {
        public RateLimit {
            rules = rules == null ? List.of() : rules;
        }
    }

    /** Allow {@code capacity} POST requests per {@code period}, per client IP, on exactly this path. */
    public record Rule(String path, int capacity, Duration period) {
    }
}