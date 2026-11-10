package com.bit_rhythm.user_service.profile.dto;

import com.bit_rhythm.user_service.profile.UserProfile;

/**
 * What another user sees. Hint: for a PRIVATE profile (see UserSettings) fill only userId, displayName,
 * avatarUrl and verified (use null for bio, country and the counters), and set restricted = true.
 */
public record PublicProfileResponse(
        Long userId,
        String displayName,
        String bio,
        String country,
        String avatarUrl,
        boolean verified,
        Long followerCount,
        Long followingCount,
        boolean following,       // does the viewer follow this user?
        boolean restricted       // true when the profile is private and details are hidden
) {
    public static PublicProfileResponse from(UserProfile profile, String avatarUrl, boolean following) {
        return new PublicProfileResponse(profile.getUserId(), profile.getDisplayName(), profile.getBio(),
                profile.getCountry(), avatarUrl, profile.isVerified(),
                profile.getFollowerCount(), profile.getFollowingCount(), following, false);
    }
}
