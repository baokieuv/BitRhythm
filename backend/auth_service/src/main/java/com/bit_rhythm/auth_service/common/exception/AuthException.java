package com.bit_rhythm.auth_service.common.exception;

import com.bit_rhythm.core.exception.ApiException;

public class AuthException extends ApiException {

    public AuthException(AuthCode code) {
        super(code);
    }

    public AuthException(AuthCode code, Object data) {
        super(code, data);
    }
}
