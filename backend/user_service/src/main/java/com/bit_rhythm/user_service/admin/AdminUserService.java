package com.bit_rhythm.user_service.admin;

import com.bit_rhythm.user_service.profile.ProfileStatus;
import com.bit_rhythm.user_service.profile.dto.ProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    /** [S2] TODO(hint): status == null -> findAll, otherwise ProfileRepository.findByStatus; sort by userId desc; page size <= 100. */
    public Page<ProfileResponse> search(ProfileStatus status, int page, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): status HIDDEN (404 PROFILE_NOT_FOUND if missing); idempotent. Hidden profiles disappear from lookups and search. */
    public void hide(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): status ACTIVE again, but NOT for DELETED profiles. */
    public void unhide(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }
}
