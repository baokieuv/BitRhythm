package com.bit_rhythm.user_service.avatar.dto;

import java.time.Instant;

/** The client PUTs the file to uploadUrl (with the same Content-Type), then calls confirm with key. */
public record UploadUrlResponse(String uploadUrl, String key, Instant expiresAt) {
}
