package com.bit_rhythm.user_service.common.exception;

import com.bit_rhythm.core.exception.ApiException;
import com.bit_rhythm.core.exception.ApiResponseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Business error. Also non-retryable for the Kafka consumer (see KafkaConfig). */
@Getter
public class UserException extends ApiException {

    private final String detail;

    public UserException(UserErrorCode code) {
        this(code, null);
    }

    public UserException(UserErrorCode code, String detail) {
        super(code);
        this.detail = detail;
    }

    @Override
    public String getMessage() {
        String base = getErrorCode().getMessage();
        return detail == null ? base : base + ": " + detail;
    }
}