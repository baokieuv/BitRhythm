package com.bit_rhythm.user_service.profile.dto;

import com.bit_rhythm.user_service.profile.UserProfile;

/** What the owner sees of their own profile. */
public record ProfileResponse(
        Long userId,
        String displayName,
        String bio,
        String country,
        String avatarUrl,
        boolean verified,
        long followerCount,
        long followingCount
) {
    /** avatarUrl comes from StorageClient.publicUrl(profile.getAvatarKey()), null when there is no avatar. */
    public static ProfileResponse from(UserProfile profile, String avatarUrl) {
        return new ProfileResponse(profile.getUserId(), profile.getDisplayName(), profile.getBio(),
                profile.getCountry(), avatarUrl, profile.isVerified(),
                profile.getFollowerCount(), profile.getFollowingCount());
    }
}
