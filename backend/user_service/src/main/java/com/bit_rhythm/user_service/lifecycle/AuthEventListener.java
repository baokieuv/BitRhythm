package com.bit_rhythm.user_service.lifecycle;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * [S2] Consumes the account events published by auth-service (it does not publish them yet: add USER_DELETED / USER_DISABLED
 * to its outbox, on user.topics.auth-events). The value arrives as a JSON String.
 */
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final AccountLifecycleService lifecycleService;

    /**
     * TODO(hint): parse the JSON (eventId, eventType, userId), then lifecycleService.handle(...). An exception triggers the
     * retry/backoff policy of KafkaConfig. Unknown eventType values must be IGNORED (return normally), not fail:
     * auth-service will add more event types later.
     */
    @KafkaListener(topics = "${user.topics.auth-events}")
    public void onEvent(String json) {
        throw new UnsupportedOperationException("TODO");
    }
}
