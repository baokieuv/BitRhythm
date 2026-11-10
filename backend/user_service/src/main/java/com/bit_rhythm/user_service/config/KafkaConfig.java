package com.bit_rhythm.user_service.config;

import com.bit_rhythm.user_service.common.exception.UserException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

/** [S2] error handler cho consumer: retry + backoff, không DLQ. */
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(UserProperties properties) {
        UserProperties.Kafka kafka = properties.kafka();

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(kafka.maxRetries());
        backOff.setInitialInterval(kafka.initialBackoff().toMillis());
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(kafka.maxBackoff().toMillis());

        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);
        // Bad data (unknown template, missing variable, wrong credentials...): retrying cannot fix it, give up at once.
        // DeserializationException (malformed message) is already treated as non-retryable by DefaultErrorHandler.
        handler.addNotRetryableExceptions(UserException.class);
        return handler;
    }
}
