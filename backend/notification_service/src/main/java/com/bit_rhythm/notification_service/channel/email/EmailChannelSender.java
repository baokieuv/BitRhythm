package com.bit_rhythm.notification_service.channel.email;

import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.channel.ChannelSender;
import com.bit_rhythm.notification_service.channel.DeliveryResult;
import com.bit_rhythm.notification_service.channel.RenderedContent;
import com.bit_rhythm.notification_service.core.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailChannelSender implements ChannelSender {

    private final EmailProvider emailProvider;

    @Override
    public Channel channel() {
        return Channel.EMAIL;
    }

    @Override
    public DeliveryResult send(NotificationRequest request, RenderedContent content) {
        String messageId = emailProvider.send(
                new EmailMessage(request.recipient().email(), content.subject(), content.body()));
        return new DeliveryResult(emailProvider.name(), messageId);
    }
}