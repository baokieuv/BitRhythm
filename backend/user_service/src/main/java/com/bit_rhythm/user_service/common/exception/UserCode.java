package com.bit_rhythm.user_service.common.exception;

import org.springframework.http.HttpStatus;

public enum UserCode implements ApiResponseCode {
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Profile not found"),
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "Cannot follow yourself"),
    USER_BLOCKED(HttpStatus.FORBIDDEN, "User is blocked"),
    AVATAR_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Avatar type not allowed"),
    AVATAR_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Avatar too large"),
    AVATAR_NOT_UPLOADED(HttpStatus.UNPROCESSABLE_ENTITY, "Avatar not uploaded"),
    VERIFICATION_ALREADY_PENDING(HttpStatus.CONFLICT, "Verification request already pending"),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "User already verified");

    private final HttpStatus status;
    private final String message;

    UserCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return name(); }
    @Override public String message() { return message; }
}
