package com.bit_rhythm.content_service.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Two MinIO clients, because the same MinIO is reached by two different addresses:
 *
 * <ul>
 *   <li><b>minioClient</b> (primary): the server-side address, inside docker "<a href="http://minio:9000">...</a>". Used for the calls
 *       content-service makes itself: statObject, removeObject.</li>
 *   <li><b>minioPublicClient</b>: the address a BROWSER can reach, e.g. "http://localhost:9000". Used ONLY to sign presigned
 *       URLs (upload of audio and covers, streaming). The host is part of the signature, so a URL signed with "minio:9000"
 *       would not work from a browser. This client never sends a request.</li>
 * </ul>
 *
 * Inject the second one with {@code @Qualifier("minioPublicClient")}.
 */
@Configuration
public class MinioConfig {

    /** MinIO's default region. Set explicitly so the SDK never calls the (unreachable) public endpoint to discover it. */
    private static final String REGION = "us-east-1";

    @Bean
    @Primary
    public MinioClient minioClient(ContentProperties properties) {
        ContentProperties.Minio minio = properties.minio();
        require(minio.endpoint(), "endpoint");
        return build(minio.endpoint(), minio);
    }

    @Bean
    public MinioClient minioPublicClient(ContentProperties properties) {
        ContentProperties.Minio minio = properties.minio();
        require(minio.publicEndpoint(), "public-endpoint");
        return build(minio.publicEndpoint(), minio);
    }

    private static MinioClient build(String endpoint, ContentProperties.Minio minio) {
        require(minio.accessKey(), "access-key");
        require(minio.secretKey(), "secret-key");
        return MinioClient.builder()
                .endpoint(endpoint.trim())
                .credentials(minio.accessKey(), minio.secretKey())
                .region(REGION)
                .build();
    }

    /** Fail at startup with a clear message instead of at the first upload. */
    private static void require(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("content.minio." + property
                    + " is required (env MINIO_ENDPOINT, MINIO_PUBLIC_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY)");
        }
    }
}
