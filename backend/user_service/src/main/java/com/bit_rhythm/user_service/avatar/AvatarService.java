package com.bit_rhythm.user_service.avatar;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.avatar.dto.ConfirmAvatarRequest;
import com.bit_rhythm.user_service.avatar.dto.UploadUrlRequest;
import com.bit_rhythm.user_service.avatar.dto.UploadUrlResponse;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.avatar.storage.StoredObject;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.config.UserProperties;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.profile.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarService {

    private final StorageClient storageClient;
    private final ProfileRepository profileRepository;
    private final UserProperties userProperties;

    /**
     * 1) contentType must be in user.avatar.allowed-types (AVATAR_TYPE_NOT_ALLOWED), sizeBytes <= max-bytes
     * (AVATAR_TOO_LARGE). 2) key = "avatars/" + userId + "/" + UUID + "." + extension chosen from the content type (never
     * from user input). 3) storageClient.presignUpload(key, contentType, presign-ttl). Nothing is saved in the DB yet.
     */
    public UploadUrlResponse createUploadUrl(Long userId, UploadUrlRequest request) {

        if (!profileRepository.existsByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new UserException(UserErrorCode.PROFILE_NOT_FOUND);
        }

        String contentType = request.contentType();

        // 1. Validate type and size.
        if (contentType == null || !userProperties.avatar().allowedTypes().contains(contentType)) {
            throw new UserException(UserErrorCode.AVATAR_TYPE_NOT_ALLOWED);
        }
        if (request.sizeBytes() > userProperties.avatar().maxBytes()) {
            throw new UserException(UserErrorCode.AVATAR_TOO_LARGE);
        }

        // 2. Build the key. The extension comes from the content type, never from user input.
        String extension = extensionFor(contentType);
        String key = "avatars/" + userId + "/" + UUID.randomUUID() + "." + extension;

        // 3. Presign the upload. Nothing is saved in the DB yet.
        String uploadUrl = storageClient.presignUpload(key, contentType, userProperties.avatar().presignTtl());

        return new UploadUrlResponse(uploadUrl, key, Instant.now().plus(userProperties.avatar().presignTtl()));
    }

    /**
     * the key must start with "avatars/" + userId + "/" (otherwise anyone could claim someone else's
     * object); storageClient.stat(key) must exist (AVATAR_NOT_UPLOADED) and its size/content type must respect the
     * limits again (the client could have uploaded anything). Then set profile.avatarKey, and delete the previous object
     * AFTER the DB update succeeded.
     */
    public ProfileResponse confirm(Long userId, ConfirmAvatarRequest request) {

        UserProfile profile = profileRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        String newKey = request.key();

        // 1. The key must belong to this user, otherwise someone could claim another user's object.
        String requiredPrefix = "avatars/" + userId + "/";
        if (newKey == null || !newKey.startsWith(requiredPrefix) || newKey.contains("..")) {
            throw new UserException(UserErrorCode.AVATAR_INVALID);
        }

        // 2. The object must really exist in storage.
        StoredObject stored = storageClient.stat(newKey)
                .orElseThrow(() -> new UserException(UserErrorCode.AVATAR_NOT_UPLOADED));

        // 3. Re-check the limits: the client could have uploaded anything to the presigned URL.
        if (stored.size() > userProperties.avatar().maxBytes()) {
            storageClient.delete(newKey);
            throw new UserException(UserErrorCode.AVATAR_TOO_LARGE);
        }
        if (stored.contentType() == null || !userProperties.avatar().allowedTypes().contains(stored.contentType())) {
            storageClient.delete(newKey);
            throw new UserException(UserErrorCode.AVATAR_TYPE_NOT_ALLOWED);
        }

        // 4. Update the DB first.
        String previousKey = profile.getAvatarKey();
        profile.setAvatarKey(newKey);
        profileRepository.save(profile);

        // 5. Delete the previous object only AFTER the DB update succeeded.
        //    A failed cleanup must not fail the request (worst case: an orphaned file).
        if (previousKey != null && !previousKey.equals(newKey)) {
            try {
                storageClient.delete(previousKey);
            } catch (Exception e) {
                log.warn("Could not delete previous avatar {} for user {}", previousKey, userId, e);
            }
        }

        return ProfileResponse.from(profile, storageClient.publicUrl(newKey));
    }

    /** clear profile.avatarKey, then delete the object; ignore "not found". */
    public ProfileResponse remove(Long userId) {

        UserProfile profile = profileRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        if (profile.getAvatarKey() != null) {
            profile.setAvatarKey(null);
            profileRepository.save(profile);
            storageClient.delete(profile.getAvatarKey());
        }

        return ProfileResponse.from(profile, null);
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> throw new UserException(UserErrorCode.AVATAR_TYPE_NOT_ALLOWED);
        };
    }
}
