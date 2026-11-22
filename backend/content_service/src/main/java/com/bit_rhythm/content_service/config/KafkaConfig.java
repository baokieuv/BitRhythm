package com.bit_rhythm.content_service.config;

import com.bit_rhythm.content_service.common.exception.ContentException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * [S2] Error handling of the consumer of auth-service events: retry with exponential backoff, then log and skip
 * (no dead-letter topic). Spring Boot attaches this bean to the listener container automatically.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(ContentProperties properties) {
        ContentProperties.Kafka kafka = properties.kafka();

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(kafka.maxRetries());
        backOff.setInitialInterval(kafka.initialBackoff().toMillis());
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(kafka.maxBackoff().toMillis());

        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);
        handler.addNotRetryableExceptions(ContentException.class);   // bad data: retrying cannot fix it
        return handler;
    }
}
