package com.bit_rhythm.user_service.common.exception;

import com.bit_rhythm.core.exception.ApiResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ApiResponseCode {
    PROFILE_NOT_FOUND(false, "USER0001", "error.profile_not_found", HttpStatus.NOT_FOUND),
    CANNOT_FOLLOW_SELF(false, "USER0002", "error.cannot_follow_self", HttpStatus.BAD_REQUEST),
    USER_BLOCKED(false, "USER0003", "error.user_blocked", HttpStatus.FORBIDDEN),

    AVATAR_TYPE_NOT_ALLOWED(false, "USER0004", "error.avatar_type_not_allowed", HttpStatus.BAD_REQUEST),
    AVATAR_TOO_LARGE(false, "USER0005", "error.avatar_too_large", HttpStatus.PAYLOAD_TOO_LARGE),
    AVATAR_NOT_UPLOADED(false, "USER0006", "error.avatar_not_uploaded", HttpStatus.UNPROCESSABLE_ENTITY),

    VERIFICATION_NOT_FOUND(false, "USER0007", "error.verification_not_found", HttpStatus.NOT_FOUND),
    VERIFICATION_ALREADY_PENDING(false, "USER0008", "error.verification_already_pending", HttpStatus.CONFLICT),
    ALREADY_VERIFIED(false, "USER0009", "error.already_verified", HttpStatus.CONFLICT),
    AUTH_SERVICE_UNAVAILABLE(false, "USER0010", "error.auth_service_unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    USER_ERROR(false, "USER0011", "error.user_error", HttpStatus.BAD_REQUEST);

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
