package com.bit_rhythm.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(
        String internalApiKey,
        Map<String, String> services,
        Duration connectTimeout,
        Duration readTimeout,
        long maxRequestBodyBytes) {

    public GatewayProperties {
        services = services == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(services));
        connectTimeout = connectTimeout == null ? Duration.ofSeconds(2) : connectTimeout;
        readTimeout = readTimeout == null ? Duration.ofSeconds(15) : readTimeout;
        if (connectTimeout.isNegative() || connectTimeout.isZero()) {
            throw new IllegalArgumentException("gateway.connect-timeout must be positive");
        }
        if (readTimeout.isNegative() || readTimeout.isZero()) {
            throw new IllegalArgumentException("gateway.read-timeout must be positive");
        }
        if (maxRequestBodyBytes <= 0) {
            throw new IllegalArgumentException("gateway.max-request-body-bytes must be positive");
        }
    }
}
