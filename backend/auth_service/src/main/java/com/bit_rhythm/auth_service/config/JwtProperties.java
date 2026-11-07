package com.bit_rhythm.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Bound from the "jwt:" block of application.yml. Registered in SecurityConfig (@EnableConfigurationProperties). */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {
}