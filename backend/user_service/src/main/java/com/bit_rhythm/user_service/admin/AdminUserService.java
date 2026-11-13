package com.bit_rhythm.user_service.admin;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.config.UserProperties;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.ProfileStatus;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.profile.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserProperties properties;
    private final ProfileRepository profileRepository;

    /** Search for user profiles by status. */
    public Page<ProfileResponse> search(ProfileStatus status, int page, int size) {
        // Ensure page is 0 or greater
        int safePage = Math.max(0, page);

        // Ensure size is at least 1, but does not exceed the configured maximum
        int safeSize = Math.clamp(size, 1, properties.pagination().maxPageSize());

        Page<UserProfile> profiles = profileRepository.findByStatus(status, PageRequest.of(safePage, safeSize));

        return profiles.map(p -> ProfileResponse.from(p, null));
    }

    /** Hide a user's profile. */
    public void hide(Long userId) {

        UserProfile profile = profileRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        profile.setStatus(ProfileStatus.HIDDEN);

        profileRepository.save(profile);
    }

    /** Unhide a user's profile. */
    public void unhide(Long userId) {
        UserProfile profile = profileRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        profile.setStatus(ProfileStatus.ACTIVE);

        profileRepository.save(profile);
    }
}
