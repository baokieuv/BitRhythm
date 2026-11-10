package com.bit_rhythm.user_service.avatar.storage;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * TODO(hint): implement with the SDK you picked in the pom (see MinioConfig).
 *  - presignUpload: presigned PUT, expiry = ttl, signed with the PUBLIC endpoint;
 *  - stat: statObject on the server-side endpoint, empty when the error is "not found";
 *  - delete: removeObject, ignore "not found";
 *  - publicUrl: publicEndpoint + "/" + bucket + "/" + key (no network call).
 * Bucket: user.avatar.bucket.
 */
@Component
public class MinioStorageClient implements StorageClient {

    @Override
    public String presignUpload(String key, String contentType, Duration ttl) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Optional<StoredObject> stat(String key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String publicUrl(String key) {
        throw new UnsupportedOperationException("TODO");
    }
}
