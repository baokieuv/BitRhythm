package com.bit_rhythm.user_service.avatar.storage;

import java.time.Duration;
import java.util.Optional;

/** Everything user-service needs from MinIO. One implementation: MinioStorageClient. */
public interface StorageClient {

    /** URL the BROWSER can PUT the file to, signed for the PUBLIC endpoint, valid for ttl. */
    String presignUpload(String key, String contentType, Duration ttl);

    /** Size and content type of the stored object, empty when it does not exist (server-side endpoint). */
    Optional<StoredObject> stat(String key);

    void delete(String key);

    /** Public URL of an avatar (the avatar bucket is public-read on avatars/*). Null key -> null. */
    String publicUrl(String key);
}
