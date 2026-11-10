package com.bit_rhythm.user_service.profile.dto;

import com.bit_rhythm.user_service.profile.UserProfile;

/** Minimal card of a user: search results, follower lists, the internal API used by other services. */
public record UserSummary(Long userId, String displayName, String avatarUrl, boolean verified) {

    public static UserSummary from(UserProfile profile, String avatarUrl) {
        return new UserSummary(
                profile.getUserId(),
                profile.getDisplayName(),
                avatarUrl,
                profile.isVerified()
        );
    }
}
