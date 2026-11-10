package com.bit_rhythm.user_service.profile;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.block.BlockService;
import com.bit_rhythm.user_service.common.CurrentUser;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.follow.FollowService;
import com.bit_rhythm.user_service.profile.dto.ProfileResponse;
import com.bit_rhythm.user_service.profile.dto.PublicProfileResponse;
import com.bit_rhythm.user_service.profile.dto.UpdateProfileRequest;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
import com.bit_rhythm.user_service.settings.ProfileVisibility;
import com.bit_rhythm.user_service.settings.SettingsRepository;
import com.bit_rhythm.user_service.settings.SettingsService;
import com.bit_rhythm.user_service.settings.UserSettings;
import com.bit_rhythm.user_service.settings.dto.SettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_DISPLAY_NAME = 100;
    private static final int MAX_SUMMARY_IDS = 100;

    private final ProfileRepository profileRepository;
    private final StorageClient storageClient;
    private final FollowService followService;
    private final SettingsService userSettingsService;
    private final SettingsRepository settingsRepository;
    private final TransactionTemplate transactionTemplate;
    private final BlockService blockService;

    /**
     * The profile of the token's user. Created (together with its default settings) on the first call:
     * displayName = the part of the "email" claim before the "@".
     */
    public UserProfile getOrCreate(Jwt jwt) {
        Long userId = CurrentUser.id(jwt);
        return findActive(userId)
                .orElseGet(() -> createProfile(userId, jwt.getClaimAsString("email")));
    }

    public ProfileResponse getMine(Jwt jwt) {
        UserProfile profile = getOrCreate(jwt);
        return ProfileResponse.from(profile, storageClient.publicUrl(profile.getAvatarKey()));
    }

    public ProfileResponse updateMine(Jwt jwt, UpdateProfileRequest request) {
        UserProfile profile = getOrCreate(jwt);

        profile.update(request);
        profileRepository.save(profile);

        return ProfileResponse.from(profile, storageClient.publicUrl(profile.getAvatarKey()));
    }

    /**
     * What another user sees. 404 when the profile does not exist; [S2] also when it is HIDDEN/DELETED (except for its
     * owner). A PRIVATE profile shows only name, avatar and the verified badge, unless the viewer is the owner.
     */
    public PublicProfileResponse getPublic(Long id, Long viewerId) {
        UserProfile profile = findActive(id)
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        boolean owner = id.equals(viewerId);
        if (!owner && profile.getStatus() != ProfileStatus.ACTIVE) {
            throw new UserException(UserErrorCode.PROFILE_NOT_FOUND);   // hidden or deleted: looks like "does not exist"
        }

        boolean blocked = !owner && blockService.isBlockedEitherWay(viewerId, id);
        if (blocked) {
            throw new UserException(UserErrorCode.PROFILE_NOT_FOUND);   // blocked: looks like "does not exist"
        }

        String avatarUrl = storageClient.publicUrl(profile.getAvatarKey());
        boolean following = !owner && followService.isFollowing(viewerId, id);

        SettingsResponse settings = userSettingsService.get(id);
        boolean restricted = !owner && settings.visibility() == ProfileVisibility.PRIVATE;
        if (restricted) {
            return new PublicProfileResponse(profile.getUserId(), profile.getDisplayName(), null, null,
                    avatarUrl, profile.isVerified(), null, null, following, true);
        }
        return PublicProfileResponse.from(profile, avatarUrl, following);
    }

    /**
     * [S2] Batch lookup for other services (InternalUserController): the order of ids is kept, unknown, hidden and
     * deleted users are skipped, duplicates are ignored and at most 100 ids are read.
     * Gap: findByUserIdIn does not exclude soft-deleted rows (isDeleted). Add findByUserIdInAndIsDeleted(ids, code)
     * to ProfileRepository and use it here if soft-deleted profiles can still have status ACTIVE.
     */
    public List<UserSummary> summaries(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> wanted = ids.stream().filter(Objects::nonNull).distinct().limit(MAX_SUMMARY_IDS).toList();

        Map<Long, UserProfile> byId = profileRepository.findByUserIdIn(wanted).stream()
                .filter(profile -> profile.getStatus() == ProfileStatus.ACTIVE)
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        return wanted.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(profile -> new UserSummary(profile.getUserId(), profile.getDisplayName(),
                        storageClient.publicUrl(profile.getAvatarKey()), profile.isVerified()))
                .toList();
    }

    private Optional<UserProfile> findActive(Long userId) {
        return profileRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode());
    }

    /**
     * Profile + settings in ONE transaction. Two first requests of the same user can arrive together: the second insert
     * hits the primary key, its transaction rolls back (so no half-created profile), and the row created by the first
     * request is read again.
     */
    private UserProfile createProfile(Long userId, String email) {
        try {
            return transactionTemplate.execute(status -> {
                UserProfile created = profileRepository.saveAndFlush(new UserProfile(userId, defaultDisplayName(email, userId)));
                settingsRepository.saveAndFlush(new UserSettings(userId));
                return created;
            });
        } catch (DataIntegrityViolationException alreadyCreated) {
            return findActive(userId).orElseThrow(() -> alreadyCreated);
        }
    }

    private static String defaultDisplayName(String email, Long userId) {
        if (email != null) {
            int at = email.indexOf('@');
            String local = (at > 0 ? email.substring(0, at) : email).trim();
            if (!local.isEmpty()) {
                return local.length() <= MAX_DISPLAY_NAME ? local : local.substring(0, MAX_DISPLAY_NAME);
            }
        }
        return "user" + userId;
    }
}
