package com.bit_rhythm.notification_service.core;

import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.channel.ChannelSender;
import com.bit_rhythm.notification_service.channel.DeliveryResult;
import com.bit_rhythm.notification_service.channel.RenderedContent;
import com.bit_rhythm.notification_service.common.PiiMasker;
import com.bit_rhythm.notification_service.delivery.DeliveryStatus;
import com.bit_rhythm.notification_service.delivery.NotificationLog;
import com.bit_rhythm.notification_service.delivery.NotificationLogRepository;
import com.bit_rhythm.notification_service.delivery.RequestPayloadCodec;
import com.bit_rhythm.notification_service.template.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The pipeline, per channel: idempotency -> render -> send -> record.
 *
 * Deliberately NOT @Transactional: every log update commits on its own, so a FAILED row survives when the
 * exception is rethrown to Kafka for a retry. A retry resumes the same row (see startLog).
 *
 * The request is stored in the row (payload) until the delivery succeeds. That is what lets an admin re-send a
 * failed notification later without any dead-letter topic.
 */
@Slf4j
@Service
public class NotificationService {

    private final static int MAX_ERROR_MESSAGE_LENGTH = 512;

    private final ChannelResolver channelResolver;
    private final NotificationLogRepository logRepository;
    private final TemplateService templateService;
    private final RequestPayloadCodec payloadCodec;
    private final Map<Channel, ChannelSender> senders = new EnumMap<>(Channel.class);

    public NotificationService(ChannelResolver channelResolver,
                               NotificationLogRepository logRepository,
                               TemplateService templateService,
                               RequestPayloadCodec payloadCodec,
                               List<ChannelSender> channelSenders) {
        this.channelResolver = channelResolver;
        this.logRepository = logRepository;
        this.templateService = templateService;
        this.payloadCodec = payloadCodec;
        channelSenders.forEach(sender -> senders.put(sender.channel(), sender));
    }

    public void handle(NotificationRequest request) {
        for (Channel channel : channelResolver.resolve(request)) {
            deliver(request, channel);
        }
    }

    /** Re-delivers ONE channel of an earlier request (admin resend): same pipeline, same log row. */
    public void redeliver(NotificationRequest request, Channel channel) {
        deliver(request, channel);
    }

    private void deliver(NotificationRequest request, Channel channel) {
        NotificationLog entry = startLog(request, channel);
        if (entry == null) {
            log.info("Already delivered, skipping event={} channel={}", request.eventId(), channel);
            return;
        }
        try {
            RenderedContent content = templateService.render(request, channel);
            DeliveryResult result = senders.get(channel).send(request, content);

            entry.setStatus(DeliveryStatus.SENT);
            entry.setProvider(result.provider());
            entry.setProviderMessageId(result.providerMessageId());
            entry.setSentAt(LocalDateTime.now());
            entry.setErrorCode(null);
            entry.setErrorMessage(null);
            entry.setPayload(null);   // delivered: the one-time links and the address are no longer needed
            logRepository.save(entry);
        } catch (RuntimeException ex) {
            entry.setStatus(DeliveryStatus.FAILED);
            entry.setErrorCode(ex.getClass().getSimpleName());
            entry.setErrorMessage(truncate(ex.getMessage(), MAX_ERROR_MESSAGE_LENGTH));
            logRepository.save(entry);
            throw ex;   // Kafka error handler: backoff, retry, then skip (the FAILED row can be re-sent from the admin API)
        }
    }

    /**
     * Returns the row to work on, or null when this (event, channel) was already delivered.
     * An existing PENDING/FAILED row means a previous attempt failed: resume it (that is a retry).
     */
    private NotificationLog startLog(NotificationRequest request, Channel channel) {
        Optional<NotificationLog> existing = logRepository.findByEventIdAndChannel(request.eventId(), channel);
        if (existing.isPresent()) {
            NotificationLog entry = existing.get();
            if (entry.getStatus() == DeliveryStatus.SENT) {
                return null;
            }
            entry.setAttempts(entry.getAttempts() + 1);
            entry.setStatus(DeliveryStatus.PENDING);
            if (entry.getPayload() == null) {
                entry.setPayload(serialize(request));
            }
            return logRepository.save(entry);
        }

        NotificationLog entry = NotificationLog.builder()
                .eventId(request.eventId())
                .userId(request.recipient().userId())
                .type(request.type())
                .channel(channel)
                .recipientMasked(channel == Channel.EMAIL
                        ? PiiMasker.maskEmail(request.recipient().email())
                        : "user:" + request.recipient().userId())
                .status(DeliveryStatus.PENDING)
                .attempts(1)
                .payload(serialize(request))
                .build();
        try {
            return logRepository.save(entry);
        } catch (DataIntegrityViolationException duplicate) {
            return null;   // another consumer inserted the same (event, channel) first
        }
    }

    /** Best effort: if the request cannot be serialized the delivery still goes on, it just cannot be re-sent. */
    private String serialize(NotificationRequest request) {
        try {
            return payloadCodec.write(request);
        } catch (RuntimeException e) {
            log.warn("Cannot store the payload of event {}: {}", request.eventId(), e.toString());
            return null;
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}