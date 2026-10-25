package com.bit_rhythm.auth_service.common.exception;

import com.bit_rhythm.core.exception.ApiResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthCode implements ApiResponseCode {

    SUCCESS(true, "AVT0000", "success", HttpStatus.OK),
    INTERNAL_SERVER_ERROR(false, "AVT0001", "error.internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
