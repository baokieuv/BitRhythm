package com.bit_rhythm.user_service.avatar.dto;

public record UploadUrlResponse(String uploadUrl, String key, String method, long expiresInSeconds) {}
