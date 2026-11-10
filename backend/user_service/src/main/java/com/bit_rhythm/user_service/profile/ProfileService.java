package com.bit_rhythm.user_service.profile;

import com.bit_rhythm.user_service.profile.dto.ProfileResponse;
import com.bit_rhythm.user_service.profile.dto.PublicProfileResponse;
import com.bit_rhythm.user_service.profile.dto.UpdateProfileRequest;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    /**
     * [S1] TODO(hint): return the profile of the token's user, creating it (and its UserSettings) on the first call:
     * displayName defaults to the part of the "email" claim before the "@". Two requests can arrive together:
     * catch DataIntegrityViolationException on insert and read the row again. Note: save() on an entity with an
     * assigned id first runs a SELECT; that is fine here, just keep the catch.
     */
    public UserProfile getOrCreate(Jwt jwt) {
        throw new UnsupportedOperationException("TODO: get or create the profile of the token's user");
    }

    /** [S1] TODO(hint): getOrCreate + map with avatarUrl = storageClient.publicUrl(avatarKey). */
    public ProfileResponse getMine(Jwt jwt) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): getOrCreate, profile.update(request), save, map. */
    public ProfileResponse updateMine(Jwt jwt, UpdateProfileRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): 404 PROFILE_NOT_FOUND when missing. Private profile (settings.visibility) -> restricted view,
     * unless the viewer is the owner. [S2] also 404 when status is HIDDEN/DELETED or when either user blocked the other.
     * "following" = FollowService.isFollowing(viewerId, id).
     */
    public PublicProfileResponse getPublic(Long id, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): batch lookup for other services (InternalUserController); keep the order of ids, skip unknown ones. */
    public List<UserSummary> summaries(List<Long> ids) {
        throw new UnsupportedOperationException("TODO");
    }
}
