package com.bit_rhythm.user_service.follow.dto;

import java.time.LocalDateTime;

/** A row of the followers / following lists. */
public record FollowUserResponse(Long userId, String displayName, String avatarUrl, boolean verified, LocalDateTime followedAt) {
}
