package com.bit_rhythm.auth_service.common.exception;

import org.springframework.http.HttpStatus;

/** RFC 6749 §5.2 error, rendered by {@link OAuth2ExceptionHandler}. */
public class OAuth2Exception extends RuntimeException {

    private final HttpStatus status;
    private final String error;

    public OAuth2Exception(HttpStatus status, String error, String description) {
        super(description);
        this.status = status;
        this.error = error;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public static OAuth2Exception invalidClient() {
        return new OAuth2Exception(HttpStatus.UNAUTHORIZED, "invalid_client", "Client authentication failed");
    }

    public static OAuth2Exception invalidRequest(String description) {
        return new OAuth2Exception(HttpStatus.BAD_REQUEST, "invalid_request", description);
    }

    public static OAuth2Exception invalidScope(String description) {
        return new OAuth2Exception(HttpStatus.BAD_REQUEST, "invalid_scope", description);
    }

    public static OAuth2Exception invalidTarget(String description) {
        return new OAuth2Exception(HttpStatus.BAD_REQUEST, "invalid_target", description);
    }

    public static OAuth2Exception unsupportedGrantType() {
        return new OAuth2Exception(HttpStatus.BAD_REQUEST, "unsupported_grant_type",
                "Only client_credentials is supported");
    }
}
