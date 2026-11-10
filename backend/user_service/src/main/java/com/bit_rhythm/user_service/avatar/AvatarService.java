package com.bit_rhythm.user_service.avatar;

import com.bit_rhythm.user_service.avatar.dto.ConfirmAvatarRequest;
import com.bit_rhythm.user_service.avatar.dto.UploadUrlRequest;
import com.bit_rhythm.user_service.avatar.dto.UploadUrlResponse;
import com.bit_rhythm.user_service.profile.dto.ProfileResponse;
import org.springframework.stereotype.Service;

@Service
public class AvatarService {

    /**
     * [S1] TODO(hint): 1) contentType must be in user.avatar.allowed-types (AVATAR_TYPE_NOT_ALLOWED), sizeBytes <= max-bytes
     * (AVATAR_TOO_LARGE). 2) key = "avatars/" + userId + "/" + UUID + "." + extension chosen from the content type (never
     * from user input). 3) storageClient.presignUpload(key, contentType, presign-ttl). Nothing is saved in the DB yet.
     */
    public UploadUrlResponse createUploadUrl(Long userId, UploadUrlRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): the key must start with "avatars/" + userId + "/" (otherwise anyone could claim someone else's
     * object); storageClient.stat(key) must exist (AVATAR_NOT_UPLOADED) and its size/content type must respect the
     * limits again (the client could have uploaded anything). Then set profile.avatarKey, and delete the previous object
     * AFTER the DB update succeeded.
     */
    public ProfileResponse confirm(Long userId, ConfirmAvatarRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): clear profile.avatarKey, then delete the object; ignore "not found". */
    public ProfileResponse remove(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }
}
