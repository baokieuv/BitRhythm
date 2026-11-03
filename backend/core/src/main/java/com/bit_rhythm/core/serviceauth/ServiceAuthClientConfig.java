package com.bit_rhythm.core.serviceauth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Active only in services that declare service-auth.client.token-uri (i.e. services that call other services). */
@Configuration
@EnableConfigurationProperties(ServiceAuthClientProperties.class)
@ConditionalOnProperty(prefix = "service-auth.client", name = "token-uri")
public class ServiceAuthClientConfig {

    @Bean
    public ServiceTokenProvider serviceTokenProvider(ServiceAuthClientProperties properties) {
        return new ServiceTokenProvider(properties);
    }
}
