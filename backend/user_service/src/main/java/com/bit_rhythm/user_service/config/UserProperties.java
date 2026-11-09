package com.bit_rhythm.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "user")
public record UserProperties(Avatar avatar, Minio minio, Topics topics) {

    public record Avatar(String bucket, long maxBytes, List<String> allowedTypes, Duration presignTtl) {}

    public record Minio(String endpoint, String publicEndpoint, String accessKey, String secretKey) {}

    public record Topics(String notification) {}
}
