package com.bit_rhythm.notification_service.inbox.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** SSE connections held by THIS instance. Other instances reach them through Redis (RealtimeBroadcaster). */
@Slf4j
@Component
public class SseEmitterRegistry {

    private static final long TIMEOUT_MS = 30 * 60 * 1000L;   // the browser reconnects after a timeout

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError((ex) -> remove(userId, emitter));
        return emitter;
    }

    public void send(Long userId, String json) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("inbox").data(json));
            } catch (Exception e) {
                log.warn("Failed to send SSE to user {}: {}", userId, e.getMessage());
                remove(userId, emitter);
            }
        }
    }

    /** Keeps proxies and the gateway from closing idle connections (their read timeout is shorter than this). */
    @Scheduled(fixedDelayString = "${notification.inbox.heartbeat:10s}")
    public void heartbeat() {
        emitters.forEach((userId, userEmitters) -> {
            for (SseEmitter emitter : userEmitters) {
                try {
                    emitter.send(SseEmitter.event().comment("keep-alive"));
                } catch (IOException | IllegalStateException e) {
                    remove(userId, emitter);
                }
            }
        });
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}
