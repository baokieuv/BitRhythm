package com.bit_rhythm.user_service.avatar.storage;

import java.time.Duration;
import java.util.Optional;

public interface StorageClient {
    String presignUpload(String key, String contentType, Duration ttl);
    Optional<StoredObject> stat(String key);
    void delete(String key);
    String publicUrl(String key);

    record StoredObject(long size, String contentType) {}
}
