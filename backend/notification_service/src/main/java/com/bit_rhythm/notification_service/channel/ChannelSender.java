package com.bit_rhythm.notification_service.channel;

import com.bit_rhythm.notification_service.core.NotificationRequest;

/** One implementation per channel. Throw a RuntimeException on failure: the caller records it and retries. */
public interface ChannelSender {

    Channel channel();

    DeliveryResult send(NotificationRequest request, RenderedContent content);
}
