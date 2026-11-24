package com.bit_rhythm.gateway;

import com.bit_rhythm.gateway.config.GatewayProperties;
import com.bit_rhythm.gateway.error.Causes;
import com.bit_rhythm.gateway.error.GatewayExceptionHandler;
import com.bit_rhythm.gateway.error.ProblemResponses;
import com.bit_rhythm.gateway.filter.CorrelationIdFilter;
import com.bit_rhythm.gateway.filter.HeaderOverrideRequest;
import com.bit_rhythm.gateway.filter.RequestSizeFilter;
import com.bit_rhythm.gateway.filter.RequestTooLargeException;
import com.bit_rhythm.gateway.ratelimit.RateLimitFilter;
import com.bit_rhythm.gateway.security.IdentityHeadersFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class GatewayBehaviorTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void propertiesApplyDefaultsAndMakeListsImmutable() {
        GatewayProperties properties = new GatewayProperties("key", 10, null, null, null, null);

        assertEquals(List.of(), properties.services());
        assertEquals(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
                properties.cors().allowedMethods());
        assertEquals("roles", properties.security().rolesClaim());
        assertEquals("ROLE_", properties.security().rolePrefix());
        assertFalse(properties.rateLimit().enabled());
        assertThrows(UnsupportedOperationException.class, () -> properties.services().add("auth"));
    }

    @Test
    void propertiesRejectMissingKeyAndNonPositiveBodyLimit() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GatewayProperties(" ", 10, null, null, null, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GatewayProperties("key", 0, null, null, null, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new GatewayProperties("key", 10, null, null, null,
                                new GatewayProperties.RateLimit(true, List.of(
                                        new GatewayProperties.RateLimit.Rule("r", "/api/**", null, null, 0,
                                                Duration.ofSeconds(1)))))));
    }

    @Test
    void headerOverrideIsCaseInsensitiveAndUpdatesHeaderNames() {
        MockHttpServletRequest request = request("/api/test");
        request.addHeader("X-Remove", "untrusted");
        request.addHeader("X-Keep", "original");

        HeaderOverrideRequest wrapped = new HeaderOverrideRequest(request, List.of("x-remove"),
                Map.of("X-Set", "trusted"));

        assertNull(wrapped.getHeader("X-REMOVE"));
        assertEquals("trusted", wrapped.getHeader("x-set"));
        assertEquals("original", wrapped.getHeader("X-Keep"));
        assertFalse(java.util.Collections.list(wrapped.getHeaderNames()).contains("X-Remove"));
        assertTrue(java.util.Collections.list(wrapped.getHeaderNames()).contains("X-Set"));
    }

    @Test
    void correlationFilterPreservesSafeIdAndReplacesUnsafeOrMissingId() throws Exception {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockHttpServletRequest request = request("/health");
        request.addHeader(CorrelationIdFilter.HEADER, "client-id_1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<jakarta.servlet.http.HttpServletRequest> seen = new AtomicReference<>();

        filter.doFilter(request, response, (wrapped, ignored) -> seen.set((jakarta.servlet.http.HttpServletRequest) wrapped));

        assertEquals("client-id_1", response.getHeader(CorrelationIdFilter.HEADER));
        assertEquals("client-id_1", seen.get().getHeader(CorrelationIdFilter.HEADER));
        assertEquals("client-id_1", request.getAttribute(CorrelationIdFilter.ATTRIBUTE));

        request = request("/health");
        request.addHeader(CorrelationIdFilter.HEADER, "bad value\n");
        response = new MockHttpServletResponse();
        filter.doFilter(request, response, (wrapped, ignored) ->
                assertNotEquals("bad value\n", ((jakarta.servlet.http.HttpServletRequest) wrapped)
                        .getHeader(CorrelationIdFilter.HEADER)));
        assertTrue(response.getHeader(CorrelationIdFilter.HEADER).matches("[0-9a-f-]{36}"));
    }

    @Test
    void identityFilterDropsClientHeadersAndInjectsVerifiedJwtIdentity() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("user-42").claim("roles", List.of("ADMIN")).build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_reader,spoof"))));
        MockHttpServletRequest request = request("/api/items");
        request.addHeader(IdentityHeadersFilter.USER_ID_HEADER, "attacker");
        request.addHeader(IdentityHeadersFilter.ROLES_HEADER, "ATTACKER");
        AtomicReference<jakarta.servlet.http.HttpServletRequest> seen = new AtomicReference<>();

        new IdentityHeadersFilter("ROLE_").doFilter(request, new MockHttpServletResponse(),
                (wrapped, ignored) -> seen.set((jakarta.servlet.http.HttpServletRequest) wrapped));

        assertEquals("user-42", seen.get().getHeader(IdentityHeadersFilter.USER_ID_HEADER));
        assertEquals("ADMIN", seen.get().getHeader(IdentityHeadersFilter.ROLES_HEADER));
        assertEquals("user-42", request.getAttribute(IdentityHeadersFilter.USER_ID_ATTRIBUTE));
    }

    @Test
    void identityFilterDoesNotInjectInvalidSubjectOrUnauthenticatedIdentity() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("bad\nsubject").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        MockHttpServletRequest request = request("/api/items");
        request.addHeader(IdentityHeadersFilter.USER_ID_HEADER, "attacker");
        request.addHeader(IdentityHeadersFilter.ROLES_HEADER, "attacker");
        AtomicReference<jakarta.servlet.http.HttpServletRequest> seen = new AtomicReference<>();

        new IdentityHeadersFilter("ROLE_").doFilter(request, new MockHttpServletResponse(),
                (wrapped, ignored) -> seen.set((jakarta.servlet.http.HttpServletRequest) wrapped));

        assertNull(seen.get().getHeader(IdentityHeadersFilter.USER_ID_HEADER));
        assertNull(seen.get().getHeader(IdentityHeadersFilter.ROLES_HEADER));
        assertNull(request.getAttribute(IdentityHeadersFilter.USER_ID_ATTRIBUTE));
    }

    @Test
    void requestSizeFilterAllowsLimitAndRejectsKnownOversizeBody() throws Exception {
        GatewayProperties properties = new GatewayProperties("key", 4, null, null, null, null);
        RequestSizeFilter filter = new RequestSizeFilter(properties);

        MockHttpServletRequest allowed = request("/api/items");
        allowed.setContent("1234".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse allowedResponse = new MockHttpServletResponse();
        AtomicReference<jakarta.servlet.http.HttpServletRequest> seen = new AtomicReference<>();
        filter.doFilter(allowed, allowedResponse, (wrapped, ignored) -> seen.set((jakarta.servlet.http.HttpServletRequest) wrapped));
        assertNotNull(seen.get());
        assertEquals(200, allowedResponse.getStatus());

        MockHttpServletRequest rejected = request("/api/items");
        rejected.setContent("12345".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();
        filter.doFilter(rejected, rejectedResponse, (wrapped, ignored) -> fail("oversized request reached chain"));
        assertEquals(413, rejectedResponse.getStatus());
        assertTrue(rejectedResponse.getContentAsString().contains("Request body exceeds"));
    }

    @Test
    void requestSizeFilterRejectsChunkedBodyWhileReading() throws Exception {
        GatewayProperties properties = new GatewayProperties("key", 4, null, null, null, null);
        RequestSizeFilter filter = new RequestSizeFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/items") {
            @Override
            public int getContentLength() {
                return -1;
            }

            @Override
            public long getContentLengthLong() {
                return -1L;
            }
        };
        request.addHeader(HttpHeaders.TRANSFER_ENCODING, "chunked");
        request.setContent("12345".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (wrapped, ignored) -> {
            RequestTooLargeException thrown = assertThrows(
                    RequestTooLargeException.class,
                    () -> wrapped.getInputStream().readAllBytes()
            );
            throw thrown;
        });

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("Request body exceeds"));
    }

    @Test
    void rateLimitAllowsCapacityThenReturns429AndSkipsOptions() throws Exception {
        GatewayProperties.RateLimit.Rule rule = new GatewayProperties.RateLimit.Rule(
                "items", "/api/items/**", List.of("get"), GatewayProperties.RateLimit.KeyType.IP,
                1, Duration.ofHours(1));
        RateLimitFilter filter = new RateLimitFilter(
                new GatewayProperties.RateLimit(true, List.of(rule)), new SimpleMeterRegistry());

        MockHttpServletRequest first = request("/api/items/1");
        first.setMethod("GET");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        AtomicReference<Boolean> called = new AtomicReference<>(false);
        FilterChain chain = (request, response) -> called.set(true);
        filter.doFilter(first, firstResponse, chain);
        assertTrue(called.get());
        assertEquals("1", firstResponse.getHeader("X-RateLimit-Limit"));
        assertEquals("0", firstResponse.getHeader("X-RateLimit-Remaining"));

        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(requestWithMethod("/api/items/2", "GET"), secondResponse, chain);
        assertEquals(429, secondResponse.getStatus());
        assertNotNull(secondResponse.getHeader("Retry-After"));
        assertTrue(secondResponse.getContentAsString().contains("Rate limit exceeded"));

        MockHttpServletResponse optionsResponse = new MockHttpServletResponse();
        filter.doFilter(requestWithMethod("/api/items/3", "OPTIONS"), optionsResponse,
                (request, response) -> ((MockHttpServletResponse) response).setStatus(204));
        assertEquals(204, optionsResponse.getStatus());
    }

    @Test
    void problemResponsesEscapeJsonAndIncludeCorrelationId() throws IOException {
        MockHttpServletRequest request = request("/api/test\"line");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE, "corr-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProblemResponses.write(request, response, HttpStatus.BAD_REQUEST, "bad \"value\"\n");

        assertEquals(400, response.getStatus());
        assertNotNull(response.getContentType());
        assertTrue(MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(
                MediaType.parseMediaType(response.getContentType())));
        assertTrue(response.getContentAsString().contains("\\\"value\\\""));
        assertTrue(response.getContentAsString().contains("\"correlationId\":\"corr-1\""));
    }

    @Test
    void exceptionHandlerMapsNestedUpstreamFailuresAndCorrelationId() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler();
        MockHttpServletRequest request = request("/api/items");
        request.setAttribute(CorrelationIdFilter.ATTRIBUTE, "corr-2");

        var result = handler.handleUnexpected(new IOException(new java.net.SocketTimeoutException("secret")), request);

        assertEquals(504, result.getStatusCode().value());
        ProblemDetail body = (ProblemDetail) result.getBody();
        assertEquals("Upstream service timed out", body.getDetail());
        assertEquals("corr-2", body.getProperties().get("correlationId"));
        assertFalse(body.getDetail().contains("secret"));
    }

    @Test
    void causesSearchesNestedExceptionsButStopsAtMaximumDepth() {
        assertTrue(Causes.has(new IOException(new RequestTooLargeException()), RequestTooLargeException.class));
        assertTrue(Causes.hasClassNamed(new RuntimeException(
                new IllegalStateException()), IllegalStateException.class.getName()));
        Throwable deep = new RuntimeException();
        for (int i = 0; i < 20; i++) {
            deep = new RuntimeException(deep);
        }
        assertFalse(Causes.has(deep, IllegalStateException.class));
    }

    private static MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }

    private static MockHttpServletRequest requestWithMethod(String uri, String method) {
        MockHttpServletRequest request = request(uri);
        request.setMethod(method);
        return request;
    }
}
