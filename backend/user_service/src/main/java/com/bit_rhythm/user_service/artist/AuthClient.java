package com.bit_rhythm.user_service.artist;

import com.bit_rhythm.core.serviceauth.ServiceAuthInterceptor;
import com.bit_rhythm.core.serviceauth.ServiceTokenProvider;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

/**
 * [S2] Calls auth-service with a SERVICE token (client credentials): user-service is a client registered in auth-service.
 * Needs:
 *  - in auth-service: a service client "user-service" with scope roles:write and "auth-service" in its allowed audiences
 *    (ServiceClientService.register), and the endpoint POST /internal/users/{id}/roles (TOKEN_SERVICE + scope roles:write);
 *  - here: service-auth.client.* (core's ServiceTokenProvider fetches and caches the token) and user.auth-service.base-url.
 *
 * The token is added by core's ServiceAuthInterceptor, which also retries once with a fresh token after a 401.
 */
@Slf4j
@Component
public class AuthClient {

    private static final String AUDIENCE = "auth-service";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    /** null when service-auth.client.* or the base url is not configured: the service still starts, the call fails clearly. */
    private final RestClient restClient;

    @Autowired
    public AuthClient(ObjectProvider<ServiceTokenProvider> tokenProvider,
                      @Value("${user.auth-service.base-url:}") String baseUrl) {
        ServiceTokenProvider provider = tokenProvider.getIfAvailable();
        this.restClient = provider == null || baseUrl == null || baseUrl.isBlank()
                ? null
                : buildClient(provider, baseUrl);
    }

    /** For tests: bring your own RestClient (e.g. bound to a MockRestServiceServer). */
    AuthClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * POST {auth-service}/internal/users/{userId}/roles with {"role": role}. The endpoint is idempotent: a user who already
     * has the role answers 2xx, so calling this again after a failure further on is safe.
     *
     * Failures: no answer, timeout, 5xx or any other refusal -> AUTH_SERVICE_UNAVAILABLE (the admin can retry); 404 (the user
     * does not exist in auth-service) -> PROFILE_NOT_FOUND. A 401/403 means THIS service is misconfigured (scope or audience of
     * its service client): it is logged at ERROR and also reported as AUTH_SERVICE_UNAVAILABLE.
     */
    public void grantRole(Long userId, String role) {
        if (userId == null || role == null || role.isBlank()) {
            throw new IllegalArgumentException("userId and role are required");
        }
        if (restClient == null) {
            throw new UserException(UserErrorCode.AUTH_SERVICE_UNAVAILABLE,
                    "service-auth.client.* or user.auth-service.base-url is not configured");
        }

        try {
            restClient.post()
                    .uri("/internal/users/{id}/roles", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("role", role))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {          // the server answered with 4xx / 5xx
            throw toUserException(e, userId, role);
        } catch (RestClientException | IllegalStateException e) {   // no connection, timeout, or no service token
            log.warn("Cannot reach auth-service to grant role {} to user {}: {}", role, userId, e.toString());
            throw new UserException(UserErrorCode.AUTH_SERVICE_UNAVAILABLE, e.getClass().getSimpleName());
        }
    }

    private UserException toUserException(RestClientResponseException e, Long userId, String role) {
        int status = e.getStatusCode().value();
        if (status == 404) {
            return new UserException(UserErrorCode.PROFILE_NOT_FOUND, "auth-service does not know user " + userId);
        }
        if (status == 401 || status == 403) {
            log.error("auth-service refused the service token (HTTP {}) while granting role {} to user {}: "
                    + "check the scope (roles:write) and the allowed audience (auth-service) of the service client", status, role, userId);
            return new UserException(UserErrorCode.AUTH_SERVICE_UNAVAILABLE, "auth-service refused the service token, HTTP " + status);
        }
        log.warn("auth-service answered HTTP {} while granting role {} to user {}", status, role, userId);
        return new UserException(UserErrorCode.AUTH_SERVICE_UNAVAILABLE, "HTTP " + status);
    }

    private static RestClient buildClient(ServiceTokenProvider provider, String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        return RestClient.builder()
                .baseUrl(baseUrl.trim())
                .requestFactory(requestFactory)
                .requestInterceptor(new ServiceAuthInterceptor(provider, AUDIENCE))
                .build();
    }
}