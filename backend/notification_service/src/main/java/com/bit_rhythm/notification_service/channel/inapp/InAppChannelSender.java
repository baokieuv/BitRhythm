package com.bit_rhythm.notification_service.channel.inapp;

import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.channel.ChannelSender;
import com.bit_rhythm.notification_service.channel.DeliveryResult;
import com.bit_rhythm.notification_service.channel.RenderedContent;
import com.bit_rhythm.notification_service.core.NotificationRequest;
import com.bit_rhythm.notification_service.inbox.InboxNotification;
import com.bit_rhythm.notification_service.inbox.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** "Sending" in-app = storing in the user's inbox and pushing a realtime hint to open SSE connections. */
@Component
@RequiredArgsConstructor
public class InAppChannelSender implements ChannelSender {

    private final InboxService inboxService;

    @Override
    public Channel channel() {
        return Channel.IN_APP;
    }

    @Override
    public DeliveryResult send(NotificationRequest request, RenderedContent content) {
        InboxNotification saved = inboxService.create(
                request.recipient().userId(), request.type(), content.title(), content.body());
        return new DeliveryResult("inbox", String.valueOf(saved.getId()));
    }
}
