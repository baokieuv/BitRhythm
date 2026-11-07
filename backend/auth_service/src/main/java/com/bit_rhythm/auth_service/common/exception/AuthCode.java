package com.bit_rhythm.auth_service.common.exception;

import com.bit_rhythm.core.exception.ApiResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthCode implements ApiResponseCode {

    SUCCESS(true, "AVT0000", "success", HttpStatus.OK),
    INTERNAL_SERVER_ERROR(false, "AVT0001", "error.internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CREDENTIALS(false, "AUTH0001", "auth.invalid_credentials", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(false, "AUTH0002", "auth.account_locked", HttpStatus.LOCKED),
    ACCOUNT_DISABLED(false, "AUTH0003", "auth.account_disabled", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(false, "AUTH0004", "auth.email_not_verified", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN(false, "AUTH0005", "auth.invalid_refresh_token", HttpStatus.UNAUTHORIZED),
    USER_ALREADY_EXISTS(false, "AUTH0006", "auth.user_already_exists", HttpStatus.CONFLICT),
    VALIDATION_FAILED(false, "AUTH0007", "auth.validation_failed", HttpStatus.BAD_REQUEST),
    CLIENT_ALREADY_EXISTS(false, "AUTH0008", "auth.client_already_exists", HttpStatus.CONFLICT)
    ;

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
