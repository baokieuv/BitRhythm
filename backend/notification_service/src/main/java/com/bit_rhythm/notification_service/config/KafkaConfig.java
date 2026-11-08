package com.bit_rhythm.notification_service.config;

import com.bit_rhythm.notification_service.common.exception.NotificationException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Failed records are retried with exponential backoff, then published to the dead-letter topic
 * (notification.events.DLT) instead of blocking the partition. Spring Boot attaches this
 * CommonErrorHandler bean to the listener container automatically.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                                 NotificationProperties properties) {
        NotificationProperties.Kafka kafka = properties.kafka();

        // TODO: a record that failed to DEserialize reaches the recoverer as byte[]; the JSON serializer cannot
        //       write that. If you see it, publish through a DelegatingByTypeSerializer (byte[] -> ByteArraySerializer).
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (record, ex) -> new TopicPartition(kafka.dltTopic(), -1));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(kafka.maxRetries());
        backOff.setInitialInterval(kafka.initialBackoff().toMillis());
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(kafka.maxBackoff().toMillis());

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        // Bad data (unknown template, invalid recipient...): retrying cannot fix it, go straight to the DLT.
        handler.addNotRetryableExceptions(NotificationException.class);
        return handler;
    }
}
