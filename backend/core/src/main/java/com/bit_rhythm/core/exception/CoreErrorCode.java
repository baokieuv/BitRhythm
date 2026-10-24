package com.bit_rhythm.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CoreErrorCode implements ApiResponseCode {

    // --- General & Server Errors ---
    INTERNAL_SERVER_ERROR(false, "AVT0001", "error.internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(false, "AVT0002", "error.bad_request", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(false, "AVT0003", "error.resource_not_found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(false, "AVT0004", "error.method_not_allowed", HttpStatus.METHOD_NOT_ALLOWED),
    SERVICE_UNAVAILABLE(false, "AVT0013", "error.service_unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    GATEWAY_TIMEOUT(false, "AVT0014", "error.gateway_timeout", HttpStatus.GATEWAY_TIMEOUT),
    MAINTENANCE_MODE(false, "AVT0031", "error.maintenance_mode", HttpStatus.SERVICE_UNAVAILABLE),

    // --- Authentication & Authorization ---
    UNAUTHORIZED(false, "AVT0015", "error.unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(false, "AVT0024", "error.invalid_credentials", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(false, "AVT0016", "error.token_expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(false, "AVT0017", "error.token_invalid", HttpStatus.UNAUTHORIZED),
    FORBIDDEN_ACCESS(false, "AVT0011", "error.forbidden_access", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(false, "AVT0025", "error.account_locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(false, "AVT0026", "error.account_disabled", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSIONS(false, "AVT0032", "error.insufficient_permissions", HttpStatus.FORBIDDEN),

    // --- Input, Request & Validation Errors ---
    VALIDATION_ERROR(false, "AVT0005", "error.input_validation_failed", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST_BODY(false, "AVT0006", "error.invalid_input_format", HttpStatus.BAD_REQUEST),
    MISSING_REQUEST_PARAMETER(false, "AVT0007", "error.missing_required_parameter", HttpStatus.BAD_REQUEST),
    MISSING_REQUEST_HEADER(false, "AVT0027", "error.missing_request_header", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_MEDIA_TYPE(false, "AVT0018", "error.unsupported_media_type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // --- Business Logic Errors ---
    // 422 is perfect for: "The JSON is valid, but violates a business rule" (e.g., start date > end date)
    UNPROCESSABLE_ENTITY(false, "AVT0023", "error.unprocessable_entity", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_STATE_TRANSITION(false, "AVT0028", "error.invalid_state_transition", HttpStatus.CONFLICT), // e.g., canceling an already shipped order

    // --- Data & Concurrency Errors ---
    RESOURCE_ALREADY_EXISTS(false, "AVT0019", "error.resource_already_exists", HttpStatus.CONFLICT),
    DATA_INTEGRITY_VIOLATION(false, "AVT0020", "error.data_integrity_violation", HttpStatus.CONFLICT),
    OPTIMISTIC_LOCKING_FAILURE(false, "AVT0021", "error.optimistic_locking_failure", HttpStatus.CONFLICT),

    // --- Rate Limiting & Quotas ---
    TOO_MANY_REQUESTS(false, "AVT0022", "error.too_many_requests", HttpStatus.TOO_MANY_REQUESTS),
    QUOTA_EXCEEDED(false, "AVT0029", "error.quota_exceeded", HttpStatus.FORBIDDEN), // e.g., free tier limits

    // --- File Errors ---
    FILE_IS_NOT_VALID(false, "AVT0008", "error.file_is_not_valid", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(false, "AVT0009", "error.file_too_large", HttpStatus.PAYLOAD_TOO_LARGE),
    UNSUPPORTED_FILE_TYPE(false, "AVT0033", "error.unsupported_file_type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // --- External Integrations ---
    EXTERNAL_API_ERROR(false, "AVT0010", "error.external_api_error", HttpStatus.BAD_GATEWAY),
    EXTERNAL_SERVICE_TIMEOUT(false, "AVT0030", "error.external_service_timeout", HttpStatus.GATEWAY_TIMEOUT),
    VALIDATION_SIGNATURE_ERROR(false, "AVT0012", "error.signature_validation_failed", HttpStatus.BAD_REQUEST);

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}