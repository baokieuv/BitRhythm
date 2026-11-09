package com.bit_rhythm.notification_service.inbox.realtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

@Configuration
public class RedisRealtimeConfig {

    @Bean
    public RedisMessageListenerContainer inboxListenerContainer(RedisConnectionFactory connectionFactory,
                                                                SseEmitterRegistry registry) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((message, pattern) -> {
            String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
            Long userId = Long.valueOf(channel.substring(RealtimeBroadcaster.CHANNEL_PREFIX.length()));
            registry.send(userId, new String(message.getBody(), StandardCharsets.UTF_8));
        }, new PatternTopic(RealtimeBroadcaster.CHANNEL_PREFIX + "*"));

        return container;
    }
}
