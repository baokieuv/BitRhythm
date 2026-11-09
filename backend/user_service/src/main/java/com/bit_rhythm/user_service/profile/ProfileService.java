package com.bit_rhythm.user_service.profile;

import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.common.CurrentUser;
import com.bit_rhythm.user_service.common.exception.UserCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.profile.dto.*;
import com.bit_rhythm.user_service.settings.ProfileVisibility;
import com.bit_rhythm.user_service.settings.SettingsRepository;
import com.bit_rhythm.user_service.settings.UserSettings;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profiles;
    private final SettingsRepository settings;
    private final StorageClient storage;

    public ProfileService(ProfileRepository profiles, SettingsRepository settings, StorageClient storage) {
        this.profiles = profiles;
        this.settings = settings;
        this.storage = storage;
    }

    /**
     * Tạo hồ sơ + settings mặc định ở lần gọi đầu.
     * Cố ý KHÔNG @Transactional: mỗi saveAndFlush là transaction riêng, nên bắt
     * DataIntegrityViolationException (2 request đồng thời) rồi đọc lại được.
     */
    public UserProfile getOrCreate(Jwt jwt) {
        Long id = CurrentUser.id(jwt);
        return profiles.findById(id).orElseGet(() -> {
            try {
                var p = profiles.saveAndFlush(UserProfile.create(id, CurrentUser.email(jwt)));
                ensureSettings(id);
                return p;
            } catch (DataIntegrityViolationException e) {
                ensureSettings(id);
                return profiles.findById(id).orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));
            }
        });
    }

    private void ensureSettings(Long id) {
        if (settings.existsById(id)) return;
        try {
            settings.saveAndFlush(UserSettings.defaults(id));
        } catch (DataIntegrityViolationException ignored) {
            // request kia đã tạo
        }
    }

    public ProfileResponse getMine(Jwt jwt) {
        return toMine(getOrCreate(jwt), CurrentUser.email(jwt));
    }

    @Transactional
    public ProfileResponse updateMine(Jwt jwt, UpdateProfileRequest req) {
        getOrCreate(jwt);
        var p = profiles.findById(CurrentUser.id(jwt)).orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));
        p.update(req);
        return toMine(p, CurrentUser.email(jwt));
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublic(Long id, Long viewerId) {
        var p = profiles.findById(id).orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));
        if (p.getStatus() != ProfileStatus.ACTIVE) throw new UserException(UserCode.PROFILE_NOT_FOUND); // [S2]
        // [S2] if (blockService.isBlockedEitherWay(id, viewerId)) throw new UserException(UserCode.PROFILE_NOT_FOUND);

        var visibility = settings.findById(id).map(UserSettings::getVisibility).orElse(ProfileVisibility.PUBLIC);
        boolean restricted = visibility == ProfileVisibility.PRIVATE && !id.equals(viewerId);
        if (restricted) {
            return new PublicProfileResponse(id, p.getDisplayName(), storage.publicUrl(p.getAvatarKey()),
                    null, null, null, null, null, true);
        }
        return new PublicProfileResponse(id, p.getDisplayName(), storage.publicUrl(p.getAvatarKey()),
                p.getBio(), p.getCountry(), p.isVerified(), p.getFollowerCount(), p.getFollowingCount(), false);
    }

    private ProfileResponse toMine(UserProfile p, String email) {
        return new ProfileResponse(p.getUserId(), email, p.getDisplayName(), p.getBio(), p.getCountry(),
                storage.publicUrl(p.getAvatarKey()), p.isVerified(), p.getFollowerCount(), p.getFollowingCount());
    }
}
