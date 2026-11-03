package com.bit_rhythm.core.serviceauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches service tokens with the client_credentials grant and caches one per target audience
 * until shortly before they expire. Thread-safe: concurrent callers share a single refresh.
 */
public class ServiceTokenProvider {

    private static final Duration REFRESH_MARGIN = Duration.ofSeconds(30);

    private final RestClient restClient = RestClient.create();
    private final ServiceAuthClientProperties properties;
    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public ServiceTokenProvider(ServiceAuthClientProperties properties) {
        this.properties = properties;
    }

    /** Returns a valid access token for calling {@code audience} (the target service name). */
    public String getAccessToken(String audience) {
        CachedToken cached = cache.get(audience);
        if (cached != null && cached.isUsable()) {
            return cached.value();
        }
        return cache.compute(audience,
                (key, existing) -> existing != null && existing.isUsable() ? existing : fetch(key)).value();
    }

    /** Drops the cached token (e.g. after a 401) so the next call fetches a new one. */
    public void invalidate(String audience) {
        cache.remove(audience);
    }

    private CachedToken fetch(String audience) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("audience", audience);
        if (properties.scope() != null && !properties.scope().isBlank()) {
            form.add("scope", properties.scope());
        }

        TokenResponse response = restClient.post()
                .uri(properties.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                // RFC 6749 §2.3.1: form-urlencode id and secret before HTTP Basic
                .headers(headers -> headers.setBasicAuth(
                        urlEncode(properties.clientId()), urlEncode(properties.clientSecret())))
                .body(form)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("Auth service returned no access token for audience " + audience);
        }
        return new CachedToken(response.accessToken(), Instant.now().plusSeconds(response.expiresIn()));
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record CachedToken(String value, Instant expiresAt) {
        boolean isUsable() {
            return Instant.now().plus(REFRESH_MARGIN).isBefore(expiresAt);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn) {
    }
}
