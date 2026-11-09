package com.bit_rhythm.auth_service.event;

import com.bit_rhythm.auth_service.common.enums.Purpose;
import com.bit_rhythm.auth_service.config.AppProperties;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.core.outbox.EventSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private static final int EVENT_VERSION = 1;
    private static final String SOURCE = "auth-service";

    private final AuthOutboxRepository authOutboxRepository;
    private final AppProperties appProperties;

    // REQUIRED: joins the transaction of the caller (the one that saves the user / the verification token), so the
    // outbox row is committed or rolled back together with them.
    @Transactional(propagation = Propagation.REQUIRED)
    public void publishVerificationNotification(
            User user, String rawToken, Purpose purpose) {

        boolean passwordReset = purpose == Purpose.PASSWORD_RESET;

        String baseUrl = passwordReset ? appProperties.resetPasswordUrl() : appProperties.verifyEmailUrl();
        String actionUrl = baseUrl + "?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        // Names that notification-service understands (its NotificationType) and the variable its templates use.
        String eventType = passwordReset ? "PASSWORD_RESET_REQUESTED" : "EMAIL_VERIFICATION_REQUESTED";
        String urlKey = passwordReset ? "resetUrl" : "verifyUrl";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(urlKey, actionUrl);
        if (user.getUsername() != null) {
            data.put("username", user.getUsername());
        }

        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),
                eventType,
                EVENT_VERSION,
                Instant.now(),
                // locale: null makes notification-service use English. Pass the user's language here once User has one.
                new NotificationEvent.Recipient(user.getId(), user.getEmail(), null),
                data,
                new NotificationEvent.Metadata(SOURCE, MDC.get("correlationId")));

        try {
            // 1. Serialize the payload
            String payload = EventSerializer.serialize(event);

            // 2. Create the Outbox entity
            AuthOutboxEntity outboxEntity = new AuthOutboxEntity(
                    null,                  // Auto-generated ID
                    "User",                // Aggregate Type
                    user.getId().toString(), // Aggregate ID
                    eventType,
                    payload,
                    0,                     // is_processed = 0
                    LocalDateTime.now()
            );

            // 3. Save to database in the same transaction
            authOutboxRepository.save(outboxEntity);
            log.info("Saved {} event {} to outbox for user id={}", eventType, event.eventId(), user.getId());

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize or save notification event to outbox", ex);
        }
    }
}