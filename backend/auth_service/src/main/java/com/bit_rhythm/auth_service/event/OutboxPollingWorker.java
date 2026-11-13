package com.bit_rhythm.auth_service.event;

import com.bit_rhythm.auth_service.common.enums.AggregateType;
import com.bit_rhythm.auth_service.config.AppProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@AllArgsConstructor
public class OutboxPollingWorker {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final AuthOutboxRepository outboxRepository;
    /** String values: the producer must use StringSerializer (spring.kafka.producer.value-serializer). */
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AppProperties appProperties;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<AuthOutboxEntity> events = outboxRepository.findAndLockUnpublishedEvents();

        if (events.isEmpty()) {
            return;
        }

        for (AuthOutboxEntity event : events) {
            try {
                // Key = aggregate id (the user): the events of one user stay in order on the same partition.
                // The value is the JSON stored in the outbox row, sent as it is.
                // A timeout, so an unreachable broker cannot hold this transaction (and its row locks) forever.
                // If it times out but the message did arrive, the retry is a duplicate: notification-service
                // ignores it (same eventId).
                switch (AggregateType.fromName(event.getAggregateType())) {
                    case USER -> kafkaTemplate.send(appProperties.topics().authEvents(), event.getAggregateId(), event.getPayload())
                            .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    case NOTIFICATION -> kafkaTemplate.send(appProperties.topics().notification(), event.getAggregateId(), event.getPayload())
                            .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    case null -> {}
                }

                // Mark the event as processed
                event.setIsProcessed(1);
                outboxRepository.save(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Outbox polling interrupted at event id={}", event.getEventId());
                return;
            } catch (Exception e) {
                // Stays unprocessed and is picked up again by the next run; continue with the next event
                log.error("Failed to publish outbox event id={}: {}", event.getEventId(), e.toString());
            }
        }
    }
}