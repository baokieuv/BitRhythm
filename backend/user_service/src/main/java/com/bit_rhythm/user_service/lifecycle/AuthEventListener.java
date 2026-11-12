package com.bit_rhythm.user_service.lifecycle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * [S2] Consumes the account events published by auth-service (it does not publish them yet: add USER_DELETED / USER_DISABLED
 * to its outbox, on user.topics.auth-events). The value arrives as a JSON String.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final AccountLifecycleService lifecycleService;
    private final ObjectMapper objectMapper;

    /** Handles incoming auth events from Kafka. */
    @KafkaListener(topics = "${user.topics.auth-events}")
    public void onEvent(String json) {
        AuthEvent event;
        try {
            event = objectMapper.readValue(json, AuthEvent.class);
        } catch (RuntimeException e) {
            // no payload in the log: it holds email addresses
            log.error("Ignoring an auth event that is not valid JSON ({} characters): {}",
                    json == null ? 0 : json.length(), e.getClass().getSimpleName());
            return;
        }
        if (event == null || isBlank(event.eventId()) || isBlank(event.eventType()) || event.userId() == null) {
            log.error("Ignoring an auth event without eventId, eventType or userId");
            return;
        }
        lifecycleService.handle(event);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
