package com.bit_rhythm.user_service.avatar.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class MinioStorageClient implements StorageClient {

    private static final Set<String> NOT_FOUND_CODES = Set.of("NoSuchKey", "NoSuchObject", "NotFound");

    /** Talks to MinIO over the internal/server-side endpoint (stat, delete). */
    private final MinioClient internalClient;
    /** Built with the PUBLIC endpoint (and an explicit region) so presigned URLs are valid for the browser. */
    private final MinioClient publicClient;
    private final String bucket;
    private final String publicEndpoint;

    public MinioStorageClient(
            @Qualifier("minioClient") MinioClient internalClient,
            @Qualifier("minioPublicClient") MinioClient publicClient,
            @Value("${user.avatar.bucket}") String bucket,
            @Value("${user.avatar.public-endpoint}") String publicEndpoint) {
        this.internalClient = internalClient;
        this.publicClient = publicClient;
        this.bucket = bucket;
        this.publicEndpoint = stripTrailingSlash(publicEndpoint);
    }

    @Override
    public String presignUpload(String key, String contentType, Duration ttl) {
        try {
            // Signed locally by the SDK (no network call as long as the public client has a region set).
            // Note: contentType is not enforced in the signature; the confirm step re-checks it via stat().
            return publicClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(key)
                            .expiry((int) ttl.toSeconds())
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to presign upload for key " + key, e);
        }
    }

    @Override
    public Optional<StoredObject> stat(String key) {
        try {
            StatObjectResponse response = internalClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(key).build());
            return Optional.of(new StoredObject(response.size(), response.contentType()));
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                return Optional.empty();
            }
            throw new IllegalStateException("Failed to stat key " + key, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stat key " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            internalClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (ErrorResponseException e) {
            if (!isNotFound(e)) {
                throw new IllegalStateException("Failed to delete key " + key, e);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete key " + key, e);
        }
    }

    @Override
    public String publicUrl(String key) {
        if (key == null) {
            return null;
        }
        return publicEndpoint + "/" + bucket + "/" + key;
    }

    private static boolean isNotFound(ErrorResponseException e) {
        String code = e.errorResponse() != null ? e.errorResponse().code() : null;
        return code != null && NOT_FOUND_CODES.contains(code);
    }

    private static String stripTrailingSlash(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}