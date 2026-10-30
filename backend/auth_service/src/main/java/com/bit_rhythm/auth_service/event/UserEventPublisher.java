package com.bit_rhythm.auth_service.event;

import com.bit_rhythm.auth_service.config.AppProperties;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.verification.VerificationService;
import com.bit_rhythm.core.outbox.EventSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final AuthOutboxRepository authOutboxRepository;
    private final AppProperties appProperties;

    // We use MANDATORY or REQUIRED so it joins the existing transaction of the calling method
    // (e.g., the method that saves the User or VerificationToken to the DB).
    @Transactional(propagation = Propagation.REQUIRED)
    public void publishVerificationNotification(
            User user, String rawToken, VerificationService.Purpose purpose) {

        String baseUrl = purpose == VerificationService.Purpose.PASSWORD_RESET
                ? appProperties.resetPasswordUrl()
                : appProperties.verifyEmailUrl();
        String actionUrl = baseUrl + "?token=" +
                URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String eventType = purpose == VerificationService.Purpose.PASSWORD_RESET
                ? "PASSWORD_RESET_REQUESTED"
                : "EMAIL_VERIFICATION_REQUESTED";
        String subject = purpose == VerificationService.Purpose.PASSWORD_RESET
                ? "Reset your BitRhythm password"
                : "Verify your BitRhythm account";

        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID().toString(),
                eventType,
                user.getEmail(),
                user.getUsername(),
                subject,
                actionUrl,
                Instant.now());

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
            log.info("Saved notification event to outbox for user: {}", user.getEmail());

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize or save notification event to outbox", ex);
        }
    }
}