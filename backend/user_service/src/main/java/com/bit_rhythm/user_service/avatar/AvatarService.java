package com.bit_rhythm.user_service.avatar;

import com.bit_rhythm.user_service.avatar.dto.*;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.common.exception.UserCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.config.UserProperties;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AvatarService {

    private static final Map<String, String> EXT = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    private final StorageClient storage;
    private final ProfileRepository profiles;
    private final UserProperties props;

    public AvatarService(StorageClient storage, ProfileRepository profiles, UserProperties props) {
        this.storage = storage;
        this.profiles = profiles;
        this.props = props;
    }

    public UploadUrlResponse requestUpload(Long userId, UploadUrlRequest req) {
        var cfg = props.avatar();
        if (!cfg.allowedTypes().contains(req.contentType()) || !EXT.containsKey(req.contentType())) {
            throw new UserException(UserCode.AVATAR_TYPE_NOT_ALLOWED);
        }
        if (req.size() > cfg.maxBytes()) throw new UserException(UserCode.AVATAR_TOO_LARGE);
        if (!profiles.existsById(userId)) throw new UserException(UserCode.PROFILE_NOT_FOUND);

        var key = "avatars/" + userId + "/" + UUID.randomUUID() + "." + EXT.get(req.contentType());
        var url = storage.presignUpload(key, req.contentType(), cfg.presignTtl());
        return new UploadUrlResponse(url, key, "PUT", cfg.presignTtl().toSeconds());
    }

    @Transactional
    public String confirm(Long userId, ConfirmAvatarRequest req) {
        var key = req.key();
        if (!key.startsWith("avatars/" + userId + "/")) throw new UserException(UserCode.AVATAR_NOT_UPLOADED);

        var obj = storage.stat(key).orElseThrow(() -> new UserException(UserCode.AVATAR_NOT_UPLOADED));
        if (obj.size() > props.avatar().maxBytes()) {
            storage.delete(key);
            throw new UserException(UserCode.AVATAR_TOO_LARGE);
        }
        if (!props.avatar().allowedTypes().contains(obj.contentType())) {
            storage.delete(key);
            throw new UserException(UserCode.AVATAR_TYPE_NOT_ALLOWED);
        }

        var profile = profiles.findById(userId).orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));
        var old = profile.getAvatarKey();
        profile.setAvatarKey(key);
        if (old != null && !old.equals(key)) storage.delete(old);
        return storage.publicUrl(key);
    }

    @Transactional
    public void remove(Long userId) {
        var profile = profiles.findById(userId).orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));
        var old = profile.getAvatarKey();
        profile.setAvatarKey(null);
        if (old != null) storage.delete(old);
    }
}
