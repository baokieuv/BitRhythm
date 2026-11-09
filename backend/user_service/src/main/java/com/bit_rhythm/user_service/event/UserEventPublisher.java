package com.bit_rhythm.user_service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bit_rhythm.user_service.config.UserProperties;
import com.bit_rhythm.user_service.event.outbox.UserOutboxEntity;
import com.bit_rhythm.user_service.event.outbox.UserOutboxRepository;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.settings.SettingsRepository;
import com.bit_rhythm.user_service.settings.UserSettings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Component
public class UserEventPublisher {

    private final UserOutboxRepository outbox;
    private final SettingsRepository settings;
    private final UserProperties props;
    private final ObjectMapper mapper;

    public UserEventPublisher(UserOutboxRepository outbox, SettingsRepository settings,
                              UserProperties props, ObjectMapper mapper) {
        this.outbox = outbox;
        this.settings = settings;
        this.props = props;
        this.mapper = mapper;
    }

    /** Ghi outbox trong CÙNG transaction với follow. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishNewFollower(UserProfile follower, UserProfile followee) {
        var locale = settings.findById(followee.getUserId()).map(UserSettings::getLocale).orElse("vi");

        // eventId xác định trước: follow/unfollow/follow lại chỉ gửi thông báo một lần (khóa event_id + channel)
        var eventId = "follow:" + follower.getUserId() + ":" + followee.getUserId();

        var event = new NotificationEvent(
                eventId, "NEW_FOLLOWER",
                new NotificationEvent.Recipient(followee.getUserId(), null, locale), // email null: chỉ kênh in-app
                Map.of("actorName", follower.getDisplayName(), "actorId", follower.getUserId()),
                Map.of("source", "user-service"),
                Instant.now());

        try {
            outbox.save(new UserOutboxEntity(String.valueOf(followee.getUserId()), eventId, "NEW_FOLLOWER",
                    props.topics().notification(), mapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event", e);
        }
    }
}
