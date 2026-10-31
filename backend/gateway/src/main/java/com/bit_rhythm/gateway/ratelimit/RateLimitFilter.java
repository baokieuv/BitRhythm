package com.bit_rhythm.gateway.ratelimit;

import com.bit_rhythm.gateway.config.GatewayProperties;
import com.bit_rhythm.gateway.config.GatewayProperties.RateLimit.KeyType;
import com.bit_rhythm.gateway.config.GatewayProperties.RateLimit.Rule;
import com.bit_rhythm.gateway.error.ProblemResponses;
import com.bit_rhythm.gateway.security.IdentityHeadersFilter;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Token-bucket rate limiting (Bucket4j) with in-memory buckets: limits are PER GATEWAY INSTANCE,
 * so with N instances the effective limit is about N times the configured capacity. Swap the bucket
 * store for a Bucket4j ProxyManager (Redis) when instances must share counters.
 * Not a Spring bean on purpose: it is added inside the security chain, after authentication.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private record Compiled(Rule rule, PathPattern pattern, Set<String> methods) {
    }

    private final List<Compiled> rules;
    private final MeterRegistry meters;
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(200_000)
            .expireAfterAccess(Duration.ofMinutes(15))
            .build();

    public RateLimitFilter(GatewayProperties.RateLimit config, MeterRegistry meters) {
        this.meters = meters;
        this.rules = !config.enabled() ? List.of() : config.rules().stream()
                .map(rule -> new Compiled(rule,
                        PathPatternParser.defaultInstance.parse(rule.path()),
                        rule.methods().stream().map(m -> m.toUpperCase(Locale.ROOT)).collect(Collectors.toSet())))
                .toList();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return rules.isEmpty() || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Compiled match = match(request);
        if (match == null) {
            filterChain.doFilter(request, response);
            return;
        }
        Rule rule = match.rule();
        String key = rule.id() + '|' + resolveKey(request, rule.key());
        Bucket bucket = buckets.get(key, ignored -> newBucket(rule));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader("X-RateLimit-Limit", String.valueOf(rule.capacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }
        long retryAfterSeconds = Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        meters.counter("gateway.rate_limit.rejected", "rule", rule.id()).increment();
        ProblemResponses.write(request, response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
    }

    private Compiled match(HttpServletRequest request) {
        PathContainer path = PathContainer.parsePath(request.getRequestURI());
        for (Compiled candidate : rules) {
            boolean methodMatches = candidate.methods().isEmpty() || candidate.methods().contains(request.getMethod());
            if (methodMatches && candidate.pattern().matches(path)) {
                return candidate;
            }
        }
        return null;
    }

    private String resolveKey(HttpServletRequest request, KeyType type) {
        if (type == KeyType.USER) {
            Object user = request.getAttribute(IdentityHeadersFilter.USER_ID_ATTRIBUTE);
            if (user != null) {
                return "user:" + user;
            }
        }
        // getRemoteAddr() already reflects X-Forwarded-For only when server.forward-headers-strategy=native
        // and the caller is a trusted proxy; never read X-Forwarded-For directly here.
        return "ip:" + request.getRemoteAddr();
    }

    private static Bucket newBucket(Rule rule) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rule.capacity())
                .refillGreedy(rule.capacity(), rule.period())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
