package com.bit_rhythm.notification_service.delivery;

import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.core.NotificationType;

import java.time.Instant;

/** What an admin sees of a delivery. The stored payload (links, email address) is deliberately NOT included. */
public record DeliveryLogResponse(
        Long id,
        String eventId,
        Long userId,
        NotificationType type,
        Channel channel,
        String recipient,
        DeliveryStatus status,
        int attempts,
        String provider,
        String providerMessageId,
        String errorCode,
        String errorMessage,
        Instant createdAt,
        Instant sentAt,
        boolean resendable
) {
    public static DeliveryLogResponse from(NotificationLog log) {
        return new DeliveryLogResponse(
                log.getId(), log.getEventId(), log.getUserId(), log.getType(), log.getChannel(),
                log.getRecipientMasked(), log.getStatus(), log.getAttempts(), log.getProvider(),
                log.getProviderMessageId(), log.getErrorCode(), log.getErrorMessage(),
                log.getCreatedAt(), log.getSentAt(),
                log.getStatus() == DeliveryStatus.FAILED && log.getPayload() != null);
    }
}