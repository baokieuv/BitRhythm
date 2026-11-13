package com.bit_rhythm.user_service.event;

import com.bit_rhythm.core.outbox.EventSerializer;
import com.bit_rhythm.user_service.common.enums.AggregateType;
import com.bit_rhythm.user_service.event.outbox.UserOutboxEntity;
import com.bit_rhythm.user_service.event.outbox.UserOutboxRepository;
import com.bit_rhythm.user_service.profile.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Writes events for other services to the outbox; OutboxPollingWorker publishes them to Kafka. */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private static final int EVENT_VERSION = 1;
    private static final String SOURCE = "user-service";

    private final UserOutboxRepository outboxRepository;

    /**
     * MANDATORY: must be called inside the transaction that inserts the follow, so the event exists if and only if the
     * follow was committed.
     *
     * The eventId is DETERMINISTIC ("follow:{follower}:{followee}"): follow, unfollow, follow again produces the same id and
     * notification-service (unique event id + channel) notifies the person only once.
     * email is null: NEW_FOLLOWER is delivered in the app only.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishNewFollower(UserProfile follower, Long followeeId, String followeeLocale) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("actorName", follower.getDisplayName());
        data.put("actorId", follower.getUserId());

        NotificationEvent event = new NotificationEvent(
                "follow:" + follower.getUserId() + ":" + followeeId,
                NotificationEventType.NEW_FOLLOWER.name(),
                EVENT_VERSION,
                Instant.now(),
                new NotificationEvent.Recipient(followeeId, null, followeeLocale),
                data,
                new NotificationEvent.Metadata(SOURCE, MDC.get("correlationId")));

        try {
            String payload = EventSerializer.serialize(event);
            outboxRepository.save(new UserOutboxEntity(
                    null,                           // generated id
                    AggregateType.NOTIFICATION.name(),     // aggregate type
                    followeeId.toString(),                 // aggregate id: events of one recipient stay in order
                    NotificationEventType.NEW_FOLLOWER.name(),
                    payload,
                    0,                          // not published yet
                    LocalDateTime.now()));
            log.info("Saved {} event {} to outbox", NotificationEventType.NEW_FOLLOWER.name(), event.eventId());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize or save the notification event to the outbox", ex);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void publishArtistVerificationResult(UserProfile user, String result, String note) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("result", result);
        data.put("note", note);

        NotificationEvent event = new NotificationEvent(
                "artist_verification:" + Instant.now().toEpochMilli() + ":" + user.getUserId(),
                NotificationEventType.ARTIST_VERIFICATION_RESULT.name(),
                EVENT_VERSION,
                Instant.now(),
                new NotificationEvent.Recipient(user.getUserId(), user.getEmail(), null),
                data,
                new NotificationEvent.Metadata(SOURCE, MDC.get("correlationId")));

        try {
            String payload = EventSerializer.serialize(event);
            outboxRepository.save(new UserOutboxEntity(
                    null,                          // generated id
                    AggregateType.NOTIFICATION.name(),          // aggregate type
                    user.getUserId().toString(),          // aggregate id: events of one recipient stay in order
                    NotificationEventType.ARTIST_VERIFICATION_RESULT.name(),
                    payload,
                    0,                          // not published yet
                    LocalDateTime.now()));
            log.info("Saved ARTIST_VERIFICATION_RESULT event {} to outbox", event.eventId());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize or save the notification event to the outbox", ex);
        }
    }
}