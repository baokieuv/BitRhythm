package com.bit_rhythm.content_service.storage;

import java.time.Duration;
import java.util.Optional;

/**
 * Everything content-service needs from MinIO. Two buckets: the PRIVATE audio bucket (content.audio.bucket) and the public
 * cover bucket (content.cover.bucket); the bucket is a parameter. One implementation: MinioStorageClient.
 */
public interface StorageClient {

    /** URL the BROWSER can PUT a file to, signed for the PUBLIC endpoint, valid for ttl. */
    String presignUpload(String bucket, String key, String contentType, Duration ttl);

    /** URL the BROWSER can GET a private object from (streaming), valid for ttl. Range requests are supported by MinIO. */
    String presignDownload(String bucket, String key, Duration ttl);

    /** Size and content type of the stored object, empty when it does not exist (server-side endpoint). */
    Optional<StoredObject> stat(String bucket, String key);

    void delete(String bucket, String key);

    /** Permanent URL of an object of a PUBLIC bucket (covers). Null key -> null. */
    String publicUrl(String bucket, String key);
}
