package com.bit_rhythm.user_service.event;

import com.bit_rhythm.core.outbox.EventSerializer;
import com.bit_rhythm.user_service.event.outbox.UserOutboxEntity;
import com.bit_rhythm.user_service.event.outbox.UserOutboxRepository;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.settings.SettingsRepository;
import com.bit_rhythm.user_service.settings.UserSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private static final int EVENT_VERSION = 1;
    private static final String SOURCE = "user-service";
    private static final String EVENT_TYPE = "NEW_FOLLOWER";

    private final UserOutboxRepository outboxRepository;
    private final SettingsRepository settings;


    /** Ghi outbox trong CÙNG transaction với follow. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishNewFollower(UserProfile follower, UserProfile followee) {
        var locale = settings.findById(followee.getUserId()).map(UserSettings::getLocale).orElse("vi");

        // eventId xác định trước: follow/unfollow/follow lại chỉ gửi thông báo một lần (khóa event_id + channel)
        var eventId = "follow:" + follower.getUserId() + ":" + followee.getUserId();

        Map<String, Object> data = Map.of("actorName", follower.getDisplayName(), "actorId", follower.getUserId());

        NotificationEvent event = new NotificationEvent(
                eventId,
                EVENT_TYPE,
                EVENT_VERSION,
                Instant.now(),
                new NotificationEvent.Recipient(followee.getUserId(), null, locale), // email null: chỉ kênh in-app
                data,
                new NotificationEvent.Metadata(SOURCE, MDC.get("correlationId")));

        try {
            // 1. Serialize the payload
            String payload = EventSerializer.serialize(event);

            // 2. Create the Outbox entity
            UserOutboxEntity outboxEntity = new UserOutboxEntity(
                    null,
                    "User" + followee.getUserId(), // aggregateId: userId của người nhận
                    eventId,
                    EVENT_TYPE,
                    payload,
                    0,
                    LocalDateTime.now()
            );

            // 3. Save to outbox table
            outboxRepository.save(outboxEntity);
            log.info("Published new follower event to outbox: {}", eventId);

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize or save notification event to outbox", ex);
        }
    }
}
