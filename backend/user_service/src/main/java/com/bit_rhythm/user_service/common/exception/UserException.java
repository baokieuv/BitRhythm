package com.bit_rhythm.user_service.common.exception;

public class UserException extends RuntimeException {
    private final transient ApiResponseCode code;

    public UserException(ApiResponseCode code) {
        super(code.message());
        this.code = code;
    }

    public ApiResponseCode getCode() { return code; }
}
