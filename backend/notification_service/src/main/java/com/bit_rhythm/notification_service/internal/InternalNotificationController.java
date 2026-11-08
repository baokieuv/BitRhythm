package com.bit_rhythm.notification_service.internal;

import com.bit_rhythm.notification_service.core.NotificationRequest;
import com.bit_rhythm.notification_service.core.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * [S2] For callers that need to send right now (instead of publishing an event).
 * Service tokens only: the caller needs scope notifications:send and "notification-service" in its allowed audiences.
 */
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAuthority('notifications:send')")
    public ResponseEntity<Void> send(@Valid @RequestBody InternalNotificationRequest request) {
        notificationService.handle(new NotificationRequest(
                request.idempotencyKey(),
                request.type(),
                request.recipient(),
                request.data() == null ? Map.of() : request.data(),
                "internal-api"));
        return ResponseEntity.accepted().build();
    }
}
