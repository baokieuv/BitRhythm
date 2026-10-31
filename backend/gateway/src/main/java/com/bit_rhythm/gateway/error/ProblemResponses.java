package com.bit_rhythm.gateway.error;

import com.bit_rhythm.gateway.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes RFC 9457 problem+json for code that runs outside Spring MVC (servlet filters, security
 * entry points). Hand-built JSON on purpose: no dependency on the Jackson version in use.
 */
public final class ProblemResponses {

    private ProblemResponses() {
    }

    public static void write(HttpServletRequest request, HttpServletResponse response,
                             HttpStatus status, String detail) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        Object correlationId = request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        StringBuilder json = new StringBuilder(256)
                .append("{\"type\":\"about:blank\"")
                .append(",\"title\":").append(quote(status.getReasonPhrase()))
                .append(",\"status\":").append(status.value())
                .append(",\"detail\":").append(quote(detail))
                .append(",\"instance\":").append(quote(request.getRequestURI()));
        if (correlationId != null) {
            json.append(",\"correlationId\":").append(quote(correlationId.toString()));
        }
        json.append('}');
        byte[] body = json.toString().getBytes(StandardCharsets.UTF_8);

        response.resetBuffer();
        response.setStatus(status.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
        response.flushBuffer();
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
