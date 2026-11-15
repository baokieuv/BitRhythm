package com.bit_rhythm.content_service.lifecycle;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * [S2] Consumes the account events published by auth-service (it does not publish them yet: add USER_DISABLED / USER_DELETED to its
 * outbox, on content.topics.auth-events). The value arrives as a JSON String.
 */
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final AccountLifecycleService lifecycleService;

    /**
     * TODO(hint): like AuthEventListener of user-service: parse the JSON into AuthEvent with the ObjectMapper (Jackson 3, tools.jackson), DROP a
     * malformed message with a log (no payload: it holds email addresses), ignore events without eventId / eventType / userId, then
     * lifecycleService.handle(event). Anything thrown by handle() goes to the retry/backoff of KafkaConfig.
     */
    @KafkaListener(topics = "${content.topics.auth-events}")
    public void onEvent(String json) {
        throw new UnsupportedOperationException("TODO");
    }
}
