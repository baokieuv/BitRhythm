package com.bit_rhythm.gateway.filter;

import com.bit_rhythm.gateway.config.GatewayProperties;
import com.bit_rhythm.gateway.security.IdentityHeadersFilter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * One access-log line + one timer per request. Never logs query string, headers or body
 * (tokens and personal data can live there). Metric tags are kept low-cardinality on purpose.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger ACCESS = LoggerFactory.getLogger("gateway.access");
    private static final String API_PREFIX = "/api/";
    private static final Set<String> KNOWN_METHODS =
            Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");

    private final MeterRegistry meters;
    private final Set<String> services;

    public AccessLogFilter(MeterRegistry meters, GatewayProperties properties) {
        this.meters = meters;
        this.services = new HashSet<>(properties.services());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.nanoTime() - start;
            int status = response.getStatus();
            String service = serviceOf(request.getRequestURI());
            Object user = request.getAttribute(IdentityHeadersFilter.USER_ID_ATTRIBUTE);
            ACCESS.info("method={} path={} status={} durationMs={} service={} ip={} user={}",
                    request.getMethod(), request.getRequestURI(), status,
                    TimeUnit.NANOSECONDS.toMillis(elapsed), service, request.getRemoteAddr(),
                    user == null ? "-" : user);
            Timer.builder("gateway.requests")
                    .tag("service", service)
                    .tag("method", KNOWN_METHODS.contains(request.getMethod()) ? request.getMethod() : "OTHER")
                    .tag("status", String.valueOf(status))
                    .register(meters)
                    .record(elapsed, TimeUnit.NANOSECONDS);
        }
    }

    private String serviceOf(String uri) {
        if (uri == null || !uri.startsWith(API_PREFIX)) {
            return "other";
        }
        String rest = uri.substring(API_PREFIX.length());
        int slash = rest.indexOf('/');
        String candidate = slash < 0 ? rest : rest.substring(0, slash);
        return services.contains(candidate) ? candidate : "other";
    }
}
