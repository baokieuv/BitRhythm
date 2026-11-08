package com.bit_rhythm.notification_service.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Business error. Also non-retryable for the Kafka consumer (see KafkaConfig). */
@Getter
public class NotificationException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public NotificationException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
