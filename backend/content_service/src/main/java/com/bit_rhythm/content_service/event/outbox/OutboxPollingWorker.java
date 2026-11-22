package com.bit_rhythm.content_service.event.outbox;

import com.bit_rhythm.content_service.config.ContentProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Publishes outbox rows to Kafka (topic content.topics.content-events). Same worker as auth-service and user-service: if you prefer,
 * move it to core and share it. Needs spring.kafka.producer.value-serializer = StringSerializer (the row already holds the JSON).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPollingWorker {

    private static final int BATCH_SIZE = 50;
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final ContentOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ContentProperties properties;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<ContentOutboxEntity> events = outboxRepository.findAndLockUnpublishedEvents(PageRequest.of(0, BATCH_SIZE));
        for (ContentOutboxEntity event : events) {
            try {
                // key = aggregate id: the events of one aggregate stay in order on one partition.
                // A duplicate after a timeout is harmless: consumers deduplicate by eventId.
                kafkaTemplate.send(properties.topics().contentEvents(), event.getAggregateId(), event.getPayload())
                        .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                event.setIsProcessed(1);
                outboxRepository.save(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}: {}", event.getEventId(), e.toString());
            }
        }
    }
}
