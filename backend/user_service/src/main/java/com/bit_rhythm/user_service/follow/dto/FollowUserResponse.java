package com.bit_rhythm.user_service.follow.dto;

public record FollowUserResponse(Long userId, String displayName, String avatarUrl, boolean verified) {}
