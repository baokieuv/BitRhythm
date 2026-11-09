package com.bit_rhythm.user_service.avatar.storage;

import com.bit_rhythm.user_service.config.UserProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class MinioStorageClient implements StorageClient {

    private final MinioClient internal;
    private final MinioClient signer;
    private final UserProperties props;

    public MinioStorageClient(@Qualifier("internalMinioClient") MinioClient internal,
                              @Qualifier("signerMinioClient") MinioClient signer,
                              UserProperties props) {
        this.internal = internal;
        this.signer = signer;
        this.props = props;
    }

    private String bucket() { return props.avatar().bucket(); }

    @Override
    public String presignUpload(String key, String contentType, Duration ttl) {
        try {
            return signer.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT).bucket(bucket()).object(key)
                    .expiry((int) ttl.toSeconds()).build());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot presign upload url", e);
        }
    }

    @Override
    public Optional<StoredObject> stat(String key) {
        try {
            var s = internal.statObject(StatObjectArgs.builder().bucket(bucket()).object(key).build());
            return Optional.of(new StoredObject(s.size(), s.contentType()));
        } catch (ErrorResponseException e) {
            return Optional.empty(); // NoSuchKey
        } catch (Exception e) {
            throw new IllegalStateException("Cannot stat object", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            internal.removeObject(RemoveObjectArgs.builder().bucket(bucket()).object(key).build());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot delete object", e);
        }
    }

    @Override
    public String publicUrl(String key) {
        if (key == null) return null;
        var base = props.minio().publicEndpoint();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + "/" + bucket() + "/" + key;
    }
}
