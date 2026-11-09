package com.bit_rhythm.user_service.common.exception;

import org.springframework.http.HttpStatus;

/** Tạm thời; thay bằng ApiResponseCode của core khi gắn vào. */
public interface ApiResponseCode {
    HttpStatus status();
    String code();
    String message();
}
