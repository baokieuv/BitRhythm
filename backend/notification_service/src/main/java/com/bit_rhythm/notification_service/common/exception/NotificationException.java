package com.bit_rhythm.notification_service.common.exception;

import com.bit_rhythm.core.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Business error. Also non-retryable for the Kafka consumer (see KafkaConfig). */
@Getter
public class NotificationException extends ApiException {

    private final HttpStatus status;
    private final String code;

    public NotificationException(HttpStatus status, String code, String message) {
        super(NotificationErrorCode.NOTIFICATION_ERROR);
        this.status = status;
        this.code = code;
    }
}
