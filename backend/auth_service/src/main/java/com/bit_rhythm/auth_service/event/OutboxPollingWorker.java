package com.bit_rhythm.auth_service.event;

import com.bit_rhythm.auth_service.config.AppProperties;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@AllArgsConstructor
public class OutboxPollingWorker {
    private final AuthOutboxRepository outboxRepository;
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
                // Publish the event to Kafka
                kafkaTemplate.send(appProperties.notificationTopic(), event.getPayload()).get();

                // Mark the event as processed
                event.setIsProcessed(1);
                outboxRepository.save(event);
            } catch (Exception e) {
                // Log the error and continue with the next event
                System.err.println("Failed to process event ID " + event.getEventId() + ": " + e.getMessage());
            }
        }
    }
}
