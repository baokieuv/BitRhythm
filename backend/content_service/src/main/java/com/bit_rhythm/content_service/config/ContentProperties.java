package com.bit_rhythm.content_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "content")
public record ContentProperties(
        Audio audio,
        Cover cover,
        Minio minio,
        Topics topics,
        Kafka kafka,
        Pagination pagination,
        Playback playback,
        Playlist playlist
) {
    /** Private bucket. uploadTtl: how long the artist has to upload; streamTtl: validity of the URL given to a listener. */
    public record Audio(String bucket, long maxBytes, List<String> allowedTypes, Duration uploadTtl, Duration streamTtl) {
    }

    /** Public-read bucket (covers/*). */
    public record Cover(String bucket, long maxBytes, List<String> allowedTypes, Duration uploadTtl) {
    }

    /** endpoint: server side (stat, delete). publicEndpoint: the host a browser can reach; presigned URLs are signed with it. */
    public record Minio(String endpoint, String publicEndpoint, String accessKey, String secretKey) {
    }

    public record Topics(String contentEvents, String authEvents) {
    }

    public record Kafka(int maxRetries, Duration initialBackoff, Duration maxBackoff) {
    }

    public record Pagination(int maxPageSize) {
    }

    public record Playback(int minPlaySeconds) {
    }

    public record Playlist(int maxTracks) {
    }
}
