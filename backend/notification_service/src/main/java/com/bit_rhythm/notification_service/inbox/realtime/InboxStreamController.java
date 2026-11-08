package com.bit_rhythm.notification_service.inbox.realtime;

import com.bit_rhythm.notification_service.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * GET /notifications/stream (text/event-stream).
 * Browsers' EventSource cannot send an Authorization header: use fetch() streaming (or a short-lived ticket).
 * Through the gateway this path needs a longer read timeout than the global 15s (or keep the heartbeat shorter).
 */
@RestController
@RequiredArgsConstructor
public class InboxStreamController {

    private final SseEmitterRegistry registry;

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal Jwt jwt) {
        return registry.register(CurrentUser.id(jwt));
    }
}
