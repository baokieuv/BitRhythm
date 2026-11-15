package com.bit_rhythm.content_service.storage;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * TODO(hint): implement with the MinIO SDK; the two clients are beans of MinioConfig:
 *  - presignUpload: getPresignedObjectUrl(Method.PUT, bucket, key, expiry) with @Qualifier("minioPublicClient");
 *  - presignDownload: the same with Method.GET (this is what makes the private audio bucket usable);
 *  - stat: statObject with the primary client, empty when the error code is "NoSuchKey";
 *  - delete: removeObject with the primary client, ignore "not found";
 *  - publicUrl: content.minio.public-endpoint + "/" + bucket + "/" + key (no network call).
 */
@Component
public class MinioStorageClient implements StorageClient {

    @Override
    public String presignUpload(String bucket, String key, String contentType, Duration ttl) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String presignDownload(String bucket, String key, Duration ttl) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Optional<StoredObject> stat(String bucket, String key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void delete(String bucket, String key) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String publicUrl(String bucket, String key) {
        throw new UnsupportedOperationException("TODO");
    }
}
