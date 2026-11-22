package com.bit_rhythm.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "user")
public record UserProperties(
        Avatar avatar,
        Minio minio,
        Topics topics,
        Kafka kafka,
        Pagination pagination
) {
    public record Avatar(String bucket, long maxBytes, List<String> allowedTypes, Duration presignTtl) {
    }

    /** endpoint: used by the server (stat, delete). publicEndpoint: the host a browser can reach; presigned URLs are signed with it. */
    public record Minio(String endpoint, String publicEndpoint, String accessKey, String secretKey) {
    }

    public record Topics(String notification, String authEvents, String userEvents) {
    }

    public record Kafka(int maxRetries, Duration initialBackoff, Duration maxBackoff) {
    }

    public record Pagination(int maxPageSize) {
    }
}
