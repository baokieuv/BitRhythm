package com.bit_rhythm.auth_service.common.exception;

import com.bit_rhythm.auth_service.token.IntrospectionController;
import com.bit_rhythm.auth_service.token.ServiceTokenController;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Token endpoints speak the OAuth2 error format (not ApiResponseFormat) so that standard OAuth2 clients
 * (Spring Security OAuth2 client, Postman, ...) understand failures.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = {ServiceTokenController.class, IntrospectionController.class})
public class OAuth2ExceptionHandler {

    @ExceptionHandler(OAuth2Exception.class)
    public ResponseEntity<ErrorBody> handle(OAuth2Exception ex) {
        ResponseEntity.BodyBuilder response = ResponseEntity.status(ex.getStatus())
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache");
        if (ex.getStatus() == HttpStatus.UNAUTHORIZED) {
            response.header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"auth-service\"");
        }
        return response.body(new ErrorBody(ex.getError(), ex.getMessage()));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorBody(String error, @JsonProperty("error_description") String errorDescription) {
    }
}