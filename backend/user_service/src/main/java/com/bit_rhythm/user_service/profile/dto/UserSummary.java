package com.bit_rhythm.user_service.profile.dto;

/** Minimal card of a user: search results, follower lists, the internal API used by other services. */
public record UserSummary(Long userId, String displayName, String avatarUrl, boolean verified) {
}
