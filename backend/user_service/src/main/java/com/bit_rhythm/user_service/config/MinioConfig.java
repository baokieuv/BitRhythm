package com.bit_rhythm.user_service.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    private static final String REGION = "us-east-1"; // cố định region để ký URL không cần gọi mạng

    /** Endpoint nội bộ: stat, delete. */
    @Bean
    public MinioClient internalMinioClient(UserProperties props) {
        var m = props.minio();
        return MinioClient.builder().endpoint(m.endpoint())
                .credentials(m.accessKey(), m.secretKey()).region(REGION).build();
    }

    /** Endpoint công khai: CHỈ để ký presigned URL (chữ ký gắn với host). */
    @Bean
    public MinioClient signerMinioClient(UserProperties props) {
        var m = props.minio();
        return MinioClient.builder().endpoint(m.publicEndpoint())
                .credentials(m.accessKey(), m.secretKey()).region(REGION).build();
    }
}
