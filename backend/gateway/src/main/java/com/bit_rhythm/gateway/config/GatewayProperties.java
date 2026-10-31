package com.bit_rhythm.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(
        String internalApiKey,
        long maxRequestBodyBytes,
        List<String> services,
        Cors cors,
        Security security,
        RateLimit rateLimit) {

    public GatewayProperties {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new IllegalArgumentException("INTERNAL_API_KEY must be configured");
        }
        if (maxRequestBodyBytes <= 0) {
            throw new IllegalArgumentException("gateway.max-request-body-bytes must be positive");
        }
        services = services == null ? List.of() : List.copyOf(services);
        cors = cors == null ? new Cors(null, null, null, false, null) : cors;
        security = security == null ? new Security(null, null, null, null, null) : security;
        rateLimit = rateLimit == null ? new RateLimit(false, null) : rateLimit;
    }

    public record Cors(List<String> allowedOrigins, List<String> allowedMethods,
                       List<String> allowedHeaders, boolean allowCredentials, Duration maxAge) {
        public Cors {
            allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
            allowedMethods = allowedMethods == null || allowedMethods.isEmpty()
                    ? List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") : List.copyOf(allowedMethods);
            allowedHeaders = allowedHeaders == null || allowedHeaders.isEmpty()
                    ? List.of("Authorization", "Content-Type", "X-Correlation-Id") : List.copyOf(allowedHeaders);
            maxAge = maxAge == null ? Duration.ofHours(1) : maxAge;
        }
    }

    /**
     * publicEndpoints entries look like "POST /api/auth/login" or just "/api/auth/health" (any method).
     */
    public record Security(List<String> publicEndpoints, String rolesClaim, String rolePrefix,
                           String adminRole, List<String> metricsAllowedCidrs) {
        public Security {
            publicEndpoints = publicEndpoints == null ? List.of() : List.copyOf(publicEndpoints);
            rolesClaim = rolesClaim == null || rolesClaim.isBlank() ? "roles" : rolesClaim;
            rolePrefix = rolePrefix == null ? "ROLE_" : rolePrefix;
            adminRole = adminRole == null || adminRole.isBlank() ? "ADMIN" : adminRole;
            metricsAllowedCidrs = metricsAllowedCidrs == null || metricsAllowedCidrs.isEmpty()
                    ? List.of("127.0.0.1/32", "::1/128", "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16")
                    : List.copyOf(metricsAllowedCidrs);
        }
    }

    public record RateLimit(boolean enabled, List<Rule> rules) {
        public RateLimit {
            rules = rules == null ? List.of() : List.copyOf(rules);
        }

        public enum KeyType { IP, USER }

        /** First matching rule wins, so order rules from most to least specific. */
        public record Rule(String id, String path, List<String> methods, KeyType key,
                           long capacity, Duration period) {
            public Rule {
                if (id == null || id.isBlank() || path == null || path.isBlank()) {
                    throw new IllegalArgumentException("gateway.rate-limit.rules[].id and path are required");
                }
                if (capacity <= 0) {
                    throw new IllegalArgumentException("rate limit capacity must be positive for rule " + id);
                }
                if (period == null || period.isZero() || period.isNegative()) {
                    throw new IllegalArgumentException("rate limit period must be positive for rule " + id);
                }
                methods = methods == null ? List.of() : List.copyOf(methods);
                key = key == null ? KeyType.IP : key;
            }
        }
    }
}
