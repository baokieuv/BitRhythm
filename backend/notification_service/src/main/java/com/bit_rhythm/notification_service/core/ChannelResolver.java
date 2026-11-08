package com.bit_rhythm.notification_service.core;

import com.bit_rhythm.notification_service.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Which channels a notification goes to: the defaults of its type, minus those the recipient cannot receive. */
@Component
public class ChannelResolver {

    public List<Channel> resolve(NotificationRequest request) {
        Recipient recipient = request.recipient();

        List<Channel> channels = new ArrayList<>(request.type().defaultChannels());
        if (recipient.email() == null || recipient.email().isBlank()) {
            channels.remove(Channel.EMAIL);
        }
        if (recipient.userId() == null) {
            channels.remove(Channel.IN_APP);
        }
        return channels;
    }
}
