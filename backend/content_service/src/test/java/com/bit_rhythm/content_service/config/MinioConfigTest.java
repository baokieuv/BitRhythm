package com.bit_rhythm.content_service.config;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Building a MinioClient does not connect to anything, so these tests need no MinIO server. */
class MinioConfigTest {

    private final MinioConfig config = new MinioConfig();

    private static ContentProperties properties(String endpoint, String publicEndpoint, String accessKey, String secretKey) {
        return new ContentProperties(null, null,
                new ContentProperties.Minio(endpoint, publicEndpoint, accessKey, secretKey), null, null, null, null, null);
    }

    @Test
    void buildsOneClientPerEndpoint() {
        ContentProperties properties = properties("http://minio:9000", "http://localhost:9000", "key", "secret");

        MinioClient server = config.minioClient(properties);
        MinioClient publicClient = config.minioPublicClient(properties);

        assertNotNull(server);
        assertNotNull(publicClient);
        assertNotSame(server, publicClient);
    }

    @Test
    void failsFastWhenTheCredentialsAreMissing() {
        ContentProperties properties = properties("http://minio:9000", "http://localhost:9000", "", "");

        assertThrows(IllegalStateException.class, () -> config.minioClient(properties));
        assertThrows(IllegalStateException.class, () -> config.minioPublicClient(properties));
    }

    @Test
    void failsFastWhenAnEndpointIsMissing() {
        ContentProperties properties = properties("http://minio:9000", " ", "key", "secret");

        assertThrows(IllegalStateException.class, () -> config.minioPublicClient(properties));
    }
}
