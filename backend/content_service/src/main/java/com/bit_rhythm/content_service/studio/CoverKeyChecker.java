package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.storage.StoredObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * A cover key comes from the client (it was issued by createCoverUploadUrl but never stored), so before using it we check that it
 * lives in the caller's own folder "covers/{userId}/", that the object really exists, and that its real type and size are allowed
 * (a signed PUT URL accepts anything).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoverKeyChecker {

    private final StorageClient storageClient;
    private final ContentProperties properties;

    /** null key = no cover: nothing to check. */
    public void check(Long userId, String coverKey) {
        if (coverKey == null) {
            return;
        }

        // somebody else's folder, or a trick: answer as if nothing was uploaded
        if (!coverKey.startsWith("covers/" + userId + "/") || coverKey.contains("..")) {
            throw new ContentException(ContentErrorCode.COVER_NOT_UPLOADED);
        }

        StoredObject object = storageClient.stat(properties.cover().bucket(), coverKey)
                .orElseThrow(() -> new ContentException(ContentErrorCode.COVER_NOT_UPLOADED));

        if (object.size() > properties.cover().maxBytes()) {
            deleteQuietly(properties.cover().bucket(), coverKey);
            throw new ContentException(ContentErrorCode.COVER_TOO_LARGE);
        }
        if (!properties.cover().allowedTypes().contains(normalizeType(object.contentType()))) {
            deleteQuietly(properties.cover().bucket(), coverKey);
            throw new ContentException(ContentErrorCode.COVER_TYPE_NOT_ALLOWED);
        }
    }

    /** "Audio/MPEG; charset=x" -> "audio/mpeg". */
    public static String normalizeType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int semicolon = contentType.indexOf(';');
        String type = semicolon >= 0 ? contentType.substring(0, semicolon) : contentType;
        return type.trim().toLowerCase(Locale.ROOT);
    }

    /** The object store is not transactional: a failed delete never fails the request. */
    private void deleteQuietly(String bucket, String key) {
        if (key == null) {
            return;
        }
        try {
            storageClient.delete(bucket, key);
        } catch (Exception e) {
            log.warn("could not delete object {}/{}: {}", bucket, key, e.toString());
        }
    }
}