package com.bit_rhythm.notification_service.event;


import com.bit_rhythm.notification_service.core.NotificationRequest;
import com.bit_rhythm.notification_service.core.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /** An exception here triggers the retry/backoff + DLT policy configured in KafkaConfig. */
    @KafkaListener(topics = "${notification.kafka.topic}")
    public void onEvent(NotificationEvent event) {
        log.info("Received event id={} type={}", event.eventId(), event.eventType());
        notificationService.handle(NotificationRequest.from(event));
    }
}
