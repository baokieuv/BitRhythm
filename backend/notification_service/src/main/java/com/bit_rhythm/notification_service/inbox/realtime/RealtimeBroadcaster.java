package com.bit_rhythm.notification_service.inbox.realtime;

import com.bit_rhythm.notification_service.core.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes "new inbox item" hints on Redis channel inbox:{userId}. Every instance listens
 * (RedisRealtimeConfig) and forwards to the SSE connections it holds, so it works with N replicas.
 */
@Component
@RequiredArgsConstructor
public class RealtimeBroadcaster {

    static final String CHANNEL_PREFIX = "inbox:";

    private final StringRedisTemplate redis;

    public void publish(Long userId, Long inboxId, NotificationType type) {
        String json = String.format("{\"id\":%d,\"type\":\"%s\"}", inboxId, type.name());
        redis.convertAndSend(CHANNEL_PREFIX + userId, json);
    }
}
