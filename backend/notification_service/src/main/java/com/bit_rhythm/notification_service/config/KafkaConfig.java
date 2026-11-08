package com.bit_rhythm.notification_service.config;

import com.bit_rhythm.notification_service.common.exception.NotificationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * A failed record is retried with exponential backoff. After the last retry it is logged at ERROR and skipped, so
 * one bad message never blocks the partition. There is no dead-letter topic: a delivery that fails is kept in
 * notification_log (status FAILED, with the request payload) and can be re-sent from the admin API.
 *
 * Spring Boot attaches this CommonErrorHandler bean to the listener container automatically.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(NotificationProperties properties) {
        NotificationProperties.Kafka kafka = properties.kafka();

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(kafka.maxRetries());
        backOff.setInitialInterval(kafka.initialBackoff().toMillis());
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(kafka.maxBackoff().toMillis());

        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);
        // Bad data (unknown template, missing variable, wrong credentials...): retrying cannot fix it, give up at once.
        // DeserializationException (malformed message) is already treated as non-retryable by DefaultErrorHandler.
        handler.addNotRetryableExceptions(NotificationException.class);
        return handler;
    }
}