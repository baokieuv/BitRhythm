package com.bit_rhythm.notification_service.admin;

import com.bit_rhythm.notification_service.common.exception.NotificationException;
import com.bit_rhythm.notification_service.core.NotificationRequest;
import com.bit_rhythm.notification_service.core.NotificationService;
import com.bit_rhythm.notification_service.delivery.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Delivery history for admins: search the log and re-send a failed notification. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDeliveryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationLogRepository logRepository;
    private final NotificationService notificationService;
    private final RequestPayloadCodec payloadCodec;

    @Transactional(readOnly = true)
    public Page<DeliveryLogResponse> search(DeliveryStatus status, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0), Math.max(1, Math.min(size, MAX_PAGE_SIZE)), Sort.by(Sort.Direction.DESC, "id"));

        Page<NotificationLog> result;
        if (userId != null && status != null) {
            result = logRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (userId != null) {
            result = logRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            result = logRepository.findByStatus(status, pageable);
        } else {
            result = logRepository.findAll(pageable);
        }
        return result.map(DeliveryLogResponse::from);
    }

    /**
     * Sends a FAILED delivery again from the request stored in its log row, through the normal pipeline
     * (same row, attempts + 1). Fix the cause first (template, credentials...), otherwise it fails again.
     * The result is the row afterwards: SENT, or FAILED with the new error.
     */
    public DeliveryLogResponse resend(Long id) {
        NotificationLog entry = logRepository.findById(id)
                .orElseThrow(() -> new NotificationException(HttpStatus.NOT_FOUND, "LOG_NOT_FOUND",
                        "Delivery log " + id + " does not exist"));

        if (entry.getStatus() == DeliveryStatus.SENT) {
            throw new NotificationException(HttpStatus.CONFLICT, "ALREADY_SENT", "This notification was already delivered");
        }
        if (entry.getPayload() == null) {
            throw new NotificationException(HttpStatus.UNPROCESSABLE_ENTITY, "PAYLOAD_NOT_AVAILABLE",
                    "The request was not stored, so it cannot be re-sent. Ask the producer to publish a new event");
        }
        NotificationRequest request = payloadCodec.read(entry.getPayload());

        // Two admins clicking at once: only one gets the row (FAILED -> PENDING), the other one gets a 409.
        if (logRepository.changeStatus(id, DeliveryStatus.FAILED, DeliveryStatus.PENDING) == 0) {
            throw new NotificationException(HttpStatus.CONFLICT, "NOT_RESENDABLE",
                    "This delivery is not in FAILED state (already being re-sent?)");
        }

        try {
            notificationService.redeliver(request, entry.getChannel());
        } catch (RuntimeException e) {
            // The pipeline already stored the failure on the row; the admin sees it in the response.
            log.warn("Re-send of log {} failed again: {}", id, e.toString());
        }

        return logRepository.findById(id).map(DeliveryLogResponse::from).orElseThrow();
    }
}