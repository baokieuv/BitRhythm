package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.exception.OAuth2Exception;
import com.bit_rhythm.auth_service.common.util.ScopeUtils;
import com.bit_rhythm.auth_service.token.dto.response.ServiceTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTokenService {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtService jwtService;

    public ServiceTokenResponse issueToken(ServiceClient client, String requestedScope, String requestedAudience) {
        List<String> scopes = resolveScopes(client, requestedScope);
        List<String> audiences = resolveAudiences(client, requestedAudience);

        JwtService.ServiceToken token = jwtService.generateServiceToken(client.getClientId(), scopes, audiences);
        return new ServiceTokenResponse(
                token.value(), TOKEN_TYPE, token.expiresInSeconds(), ScopeUtils.join(scopes));
    }

    private List<String> resolveScopes(ServiceClient client, String requestedScope) {
        List<String> granted = ScopeUtils.parse(client.getScopes());
        List<String> requested = ScopeUtils.parse(requestedScope);
        if (!new HashSet<>(granted).containsAll(requested)) {
            throw OAuth2Exception.invalidScope("Requested scope is not granted to this client");
        }
        return requested.isEmpty() ? granted : requested;
    }

    private List<String> resolveAudiences(ServiceClient client, String requestedAudience) {
        List<String> allowed = ScopeUtils.parse(client.getAllowedAudiences());
        if (allowed.isEmpty()) {
            throw OAuth2Exception.invalidTarget("Client has no allowed audience configured");
        }
        List<String> requested = ScopeUtils.parse(requestedAudience);
        if (!new HashSet<>(allowed).containsAll(requested)) {
            throw OAuth2Exception.invalidTarget("Requested audience is not allowed for this client");
        }
        return requested.isEmpty() ? allowed : requested;
    }
}
