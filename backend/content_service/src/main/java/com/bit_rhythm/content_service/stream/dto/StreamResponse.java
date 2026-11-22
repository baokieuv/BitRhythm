package com.bit_rhythm.content_service.stream.dto;

import java.time.Instant;

/** Short-lived URL to GET the audio from MinIO (the player can seek with Range requests). Ask again after expiresAt. */
public record StreamResponse(String url, Instant expiresAt) {
}
