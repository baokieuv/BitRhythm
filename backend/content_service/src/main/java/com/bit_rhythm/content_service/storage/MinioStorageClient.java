package com.bit_rhythm.content_service.storage;

import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.core.exception.ApiException;
import com.bit_rhythm.core.exception.CoreErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * StorageClient on top of the MinIO Java SDK. The two clients are the beans of MinioConfig:
 * <ul>
 *   <li>{@code minioClient} (server side, e.g. <a href="http://minio:9000">minio:9000</a>): stat and delete;</li>
 *   <li>{@code minioPublicClient} (the address of the browser, e.g. http://localhost:9000): ONLY signs presigned URLs, it never
 *       sends a request (its region is fixed in MinioConfig, so the SDK does not need the network to sign).</li>
 * </ul>
 */
@Component
public class MinioStorageClient implements StorageClient {

    /** HEAD on a missing object: MinIO answers one of these codes. */
    private static final Set<String> NOT_FOUND_CODES = Set.of("NoSuchKey", "NoSuchObject");

    private final MinioClient server;
    private final MinioClient signer;
    private final String publicEndpoint;

    public MinioStorageClient(MinioClient minioClient,
                              @Qualifier("minioPublicClient") MinioClient minioPublicClient,
                              ContentProperties properties) {
        this.server = minioClient;
        this.signer = minioPublicClient;
        this.publicEndpoint = stripTrailingSlash(properties.minio().publicEndpoint());
    }

    /**
     * Presigned PUT. The Content-Type of the request is NOT part of the signature (the SDK signs the host and the query only), so the
     * contentType argument cannot be enforced here: whoever confirms the upload must check the real type with stat().
     */
    @Override
    public String presignUpload(String bucket, String key, String contentType, Duration ttl) {
        return presign(Method.PUT, bucket, key, ttl);
    }

    /** Presigned GET of a PRIVATE object. MinIO answers Range requests, so the player can seek. */
    @Override
    public String presignDownload(String bucket, String key, Duration ttl) {
        return presign(Method.GET, bucket, key, ttl);
    }

    @Override
    public Optional<StoredObject> stat(String bucket, String key) {
        try {
            StatObjectResponse response = server.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
            return Optional.of(new StoredObject(response.size(), response.contentType()));
        } catch (ErrorResponseException e) {
            if (e.errorResponse() != null && NOT_FOUND_CODES.contains(e.errorResponse().code())) {
                return Optional.empty();
            }
            throw new ApiException(CoreErrorCode.INTERNAL_SERVER_ERROR, "Cannot stat " + bucket + "/" + key);
        } catch (Exception e) {
            throw new ApiException(CoreErrorCode.INTERNAL_SERVER_ERROR, "Cannot stat " + bucket + "/" + key);
        }
    }

    /** Download an object from the storage. */
    @Override
    public void download(String bucket, String key, Path target){
        try {
            server.downloadObject(
                    io.minio.DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .filename(target.toString())
                            .build());
        } catch (Exception e) {
            throw new ApiException(CoreErrorCode.INTERNAL_SERVER_ERROR, "Cannot download " + bucket + "/" + key);
        }
    }

    /** Deleting an object that does not exist is not an error (S3 semantics): callers can delete without checking first. */
    @Override
    public void delete(String bucket, String key) {
        try {
            server.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new ApiException(CoreErrorCode.INTERNAL_SERVER_ERROR, "Cannot delete " + bucket + "/" + key);
        }
    }

    /** Permanent URL of an object in a PUBLIC bucket (covers). Computed locally, no network call. */
    @Override
    public String publicUrl(String bucket, String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return publicEndpoint + "/" + bucket + "/" + UriUtils.encodePath(key, StandardCharsets.UTF_8);   // keeps the "/" of the key
    }

    private String presign(Method method, String bucket, String key, Duration ttl) {
        try {
            return signer.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(bucket)
                    .object(key)
                    .expiry((int) Math.max(1, ttl.toSeconds()))   // seconds, MinIO accepts up to 7 days
                    .build());
        } catch (Exception e) {
            throw new ApiException(CoreErrorCode.INTERNAL_SERVER_ERROR, "Cannot presign " + method + " " + bucket + "/" + key);
        }
    }

    private static String stripTrailingSlash(String url) {
        String trimmed = url.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}