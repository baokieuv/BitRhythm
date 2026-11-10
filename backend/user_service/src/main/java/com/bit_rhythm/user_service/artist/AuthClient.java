package com.bit_rhythm.user_service.artist;

import org.springframework.stereotype.Component;

/**
 * [S2] Calls auth-service. Needs:
 *  - a service client for "user-service" registered in auth-service (scopes: roles:write, allowed audience: auth-service);
 *  - core's ServiceTokenProvider + ServiceAuthInterceptor on a RestClient (properties service-auth.client.*, audience "auth-service");
 *  - an endpoint in auth-service: POST /internal/users/{id}/roles (TOKEN_SERVICE + scope roles:write), idempotent.
 */
@Component
public class AuthClient {

    /**
     * TODO(hint): POST {auth-service}/internal/users/{userId}/roles with body {"role": role}. Any I/O error or 5xx ->
     * UserException(AUTH_SERVICE_UNAVAILABLE) so the admin can retry; a repeated call for a role the user already has must succeed.
     */
    public void grantRole(Long userId, String role) {
        throw new UnsupportedOperationException("TODO");
    }
}
