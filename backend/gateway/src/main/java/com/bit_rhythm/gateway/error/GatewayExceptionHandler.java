package com.bit_rhythm.gateway.error;

import com.bit_rhythm.gateway.filter.CorrelationIdFilter;
import com.bit_rhythm.gateway.filter.RequestTooLargeException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Single error format for everything the gateway itself produces. Responses coming from an
 * upstream service (including its own 4xx/5xx) are forwarded untouched and never reach this class.
 */
@RestControllerAdvice
public class GatewayExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception exception, HttpServletRequest request) {
        HttpStatus status;
        String detail;

        if (Causes.has(exception, RequestTooLargeException.class)) {
            status = HttpStatus.valueOf(413);
            detail = "Request body exceeds the configured limit";
        } else if (Causes.hasClassNamed(exception, "io.github.resilience4j.circuitbreaker.CallNotPermittedException")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            detail = "Upstream service is temporarily unavailable";
        } else if (Causes.has(exception, SocketTimeoutException.class)
                || Causes.has(exception, HttpTimeoutException.class)
                || Causes.has(exception, TimeoutException.class)) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            detail = "Upstream service timed out";
        } else if (Causes.has(exception, ConnectException.class)
                || Causes.has(exception, UnknownHostException.class)
                || Causes.has(exception, NoRouteToHostException.class)) {
            status = HttpStatus.BAD_GATEWAY;
            detail = "Upstream service is unreachable";
        } else if (Causes.has(exception, IOException.class) || Causes.has(exception, RestClientException.class)) {
            status = HttpStatus.BAD_GATEWAY;
            detail = "Upstream request failed";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            detail = "Internal gateway error";
            LOG.error("Unhandled gateway error on {} {}", request.getMethod(), request.getRequestURI(), exception);
            return problem(status, detail, request);
        }
        // Never expose upstream addresses or stack traces to clients; keep the detail in the log.
        LOG.warn("{} on {} {}: {}", status.value(), request.getMethod(), request.getRequestURI(), exception.toString());
        return problem(status, detail, request);
    }

    private ResponseEntity<Object> problem(HttpStatus status, String detail, HttpServletRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(status.getReasonPhrase());
        Object correlationId = request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        if (correlationId != null) {
            body.setProperty("correlationId", correlationId.toString());
        }
        return ResponseEntity.status(status).body(body);
    }

    /** Standard Spring MVC errors (404 no route, 405, ...) get the same correlationId property. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem
                && request instanceof ServletWebRequest servletRequest) {
            Object correlationId = servletRequest.getRequest().getAttribute(CorrelationIdFilter.ATTRIBUTE);
            if (correlationId != null) {
                problem.setProperty("correlationId", correlationId.toString());
            }
        }
        return response;
    }
}
