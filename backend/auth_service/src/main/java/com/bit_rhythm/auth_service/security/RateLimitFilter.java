package com.bit_rhythm.auth_service.security;

import com.bit_rhythm.auth_service.config.AppProperties.Rule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Per-IP rate limiting with Bucket4j token buckets, configured in application.yml (app.rate-limit.rules).
 * Buckets live in memory, so each service instance counts on its own. Not a @Bean on purpose
 * (see SecurityConfig): created there with "new" and added to the security filter chain.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Rule> rulesByPath;
    private final Cache<String, Bucket> buckets;

    public RateLimitFilter(List<Rule> rules) {
        this.rulesByPath = rules.stream().collect(Collectors.toMap(Rule::path, Function.identity()));

        // A bucket idle for longer than its refill period is full again anyway, so evicting it is safe.
        Duration longestPeriod = rules.stream().map(Rule::period)
                .max(Comparator.naturalOrder()).orElse(Duration.ofMinutes(1));
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(longestPeriod.plusMinutes(1))
                .maximumSize(100_000)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !rulesByPath.containsKey(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Rule rule = rulesByPath.get(request.getRequestURI());
        // With server.forward-headers-strategy=framework, getRemoteAddr() is the real client IP.
        String key = request.getRequestURI() + '|' + request.getRemoteAddr();

        Bucket bucket = buckets.get(key, k -> newBucket(rule));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1L, (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0));
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"timestamp":"%s","status":429,"code":"RATE_LIMITED","message":"Too many requests, try again later","path":"%s"}"""
                .formatted(Instant.now(), request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"")));
    }

    private static Bucket newBucket(Rule rule) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rule.capacity())
                .refillGreedy(rule.capacity(), rule.period())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
