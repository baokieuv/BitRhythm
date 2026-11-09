package com.bit_rhythm.user_service.event.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/** Giữ giống auth-service. Value là String (StringSerializer), key là aggregateId. */
@Component
public class OutboxPollingWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxPollingWorker.class);

    private final UserOutboxRepository outbox;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPollingWorker(UserOutboxRepository outbox, KafkaTemplate<String, String> kafka) {
        this.outbox = outbox;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${user.outbox.poll-interval-ms:1000}")
    @Transactional
    public void poll() {
        for (var e : outbox.findTop50ByStatusOrderByIdAsc(OutboxStatus.PENDING)) {
            try {
                kafka.send(e.getTopic(), e.getAggregateId(), e.getPayload()).get(5, TimeUnit.SECONDS);
                e.markSent();
            } catch (Exception ex) {
                log.warn("Outbox send failed id={}, will retry: {}", e.getId(), ex.getMessage());
                return; // giữ thứ tự, thử lại ở lần poll sau
            }
        }
    }
}
