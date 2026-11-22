package com.bit_rhythm.content_service.studio.dto;

import java.time.Duration;
import java.time.Instant;

/** The client PUTs the file to uploadUrl (with the same Content-Type) before expiresAt, then confirms (audio) or sends key in the update (cover). */
public record UploadUrlResponse(String uploadUrl, String key, Instant expiresAt) {

    public static UploadUrlResponse of(String key, String url, Duration ttl) {
        return new UploadUrlResponse(url, key, Instant.now().plus(ttl));
    }
}
