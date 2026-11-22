package com.bit_rhythm.content_service.lifecycle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Consumes the account events published by auth-service (it does not publish them yet: add USER_DISABLED / USER_DELETED to its
 * outbox, on content.topics.auth-events). The value arrives as a JSON String.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final AccountLifecycleService lifecycleService;
    private final ObjectMapper objectMapper;

    /**
     * A malformed message is DROPPED with a log (never logging the payload or the parser's message: both can contain email
     * addresses); an event without eventId, eventType or userId is ignored. Anything thrown by handle() propagates to the
     * retry/backoff of KafkaConfig.
     */
    @KafkaListener(topics = "${content.topics.auth-events}")
    public void onEvent(String json) {

        AuthEvent event;
        try {
            event = objectMapper.readValue(json, AuthEvent.class);
        } catch (Exception e) {
            log.warn("Dropping malformed auth event ({})", e.getClass().getSimpleName());
            return;
        }

        if (event == null || isBlank(event.eventId()) || isBlank(event.eventType()) || event.userId() == null) {
            log.warn("Ignoring auth event without eventId, eventType or userId");
            return;
        }

        lifecycleService.handle(event);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}