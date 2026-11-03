package com.bit_rhythm.core.serviceauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credentials this service uses to call other services. */
@ConfigurationProperties(prefix = "service-auth.client")
public record ServiceAuthClientProperties(
        String tokenUri,      // e.g. http://auth-service:8081/token
        String clientId,
        String clientSecret,
        String scope          // optional, space-separated; empty = everything the client is granted
) {
}
