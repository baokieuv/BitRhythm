package com.bit_rhythm.core.exception;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.dto.FieldErrorDetail;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ResponseFactory responseFactory;

    /**
     * Handles custom business logic exceptions explicitly thrown by the application.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleApiException(ApiException ex, Locale locale) {
        ApiResponseCode errorCode = ex.getErrorCode();

        // Use log.warn for business errors to avoid cluttering error logs, but include stacktrace
        log.warn("ApiException: code={}, message='{}'", errorCode.getCode(), errorCode.getMessage(), ex);

        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                ex.getData()
        );
        return new ResponseEntity<>(responseFactory.response(errorResponse, ex.getMessageArguments()), errorCode.getHttpStatus());
    }

    /**
     * Handles @Valid / @Validated DTO binding errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        CoreErrorCode errorCode = CoreErrorCode.VALIDATION_ERROR;

        List<FieldErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        log.warn("MethodArgumentNotValidException: {}", details, ex);

        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(false, errorCode.getCode(), errorCode.getMessage(), details);
        return new ResponseEntity<>(responseFactory.response(errorResponse), errorCode.getHttpStatus());
    }

    /**
     * Handles @Validated constraint violations on single path variables or request params.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        CoreErrorCode errorCode = CoreErrorCode.VALIDATION_ERROR;

        List<FieldErrorDetail> details = ex.getConstraintViolations()
                .stream()
                .map(violation -> new FieldErrorDetail(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        log.warn("ConstraintViolationException: {}", details, ex);

        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(false, errorCode.getCode(), errorCode.getMessage(), details);
        return new ResponseEntity<>(responseFactory.response(errorResponse), errorCode.getHttpStatus());
    }

    /**
     * Handles type mismatches (e.g., passing a String to an Integer @PathVariable).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        CoreErrorCode errorCode = CoreErrorCode.BAD_REQUEST;

        String detailMessage = String.format("Parameter '%s' should be of type '%s'", ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown");

        log.warn("MethodArgumentTypeMismatchException: {}", detailMessage, ex);

        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(false, errorCode.getCode(), errorCode.getMessage(), detailMessage);
        return new ResponseEntity<>(responseFactory.response(errorResponse), errorCode.getHttpStatus());
    }

    /**
     * Handles malformed JSON requests.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        CoreErrorCode errorCode = CoreErrorCode.MALFORMED_REQUEST_BODY;
        log.warn("HttpMessageNotReadableException: Malformed JSON request body", ex);

        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(false, errorCode.getCode(), errorCode.getMessage(), null);
        return new ResponseEntity<>(responseFactory.response(errorResponse), errorCode.getHttpStatus());
    }

    /**
     * Handles missing mandatory query parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        CoreErrorCode errorCode = CoreErrorCode.MISSING_REQUEST_PARAMETER;
        Map<String, String> details = Map.of("parameterName", ex.getParameterName());

        log.warn("MissingServletRequestParameterException: {}", ex.getParameterName(), ex);

        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(false, errorCode.getCode(), errorCode.getMessage(), details);
        return new ResponseEntity<>(responseFactory.response(errorResponse), errorCode.getHttpStatus());
    }

    /**
     * Handles wrong HTTP methods (e.g., sending GET to a POST endpoint).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        CoreErrorCode errorCode = CoreErrorCode.METHOD_NOT_ALLOWED;
        log.warn("HttpRequestMethodNotSupportedException: {}", ex.getMessage(), ex);

        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(false, errorCode.getCode(), errorCode.getMessage(), null);
        return new ResponseEntity<>(responseFactory.response(errorResponse), errorCode.getHttpStatus());
    }

    /**
     * Fallback for all other unhandled exceptions (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleGenericException(Exception ex) {
        CoreErrorCode errorCode = CoreErrorCode.INTERNAL_SERVER_ERROR;

        // Log ERROR level with full stack trace for unexpected crashes
        log.error("Unhandled Exception: ", ex);

        // Security fix: Do NOT return ex.getMessage() to the client, as it may expose database queries or internal logic.
        // Use the generic message defined in CoreErrorCode instead.
        ApiResponseFormat.ApiData<Object> errorResponse = new ApiResponseFormat.ApiData<>(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );

        return new ResponseEntity<>(responseFactory.response(errorResponse), errorCode.getHttpStatus());
    }
}