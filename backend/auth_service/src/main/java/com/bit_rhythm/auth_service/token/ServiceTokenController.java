package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.exception.OAuth2Exception;
import com.bit_rhythm.auth_service.token.dto.response.ServiceTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class ServiceTokenController {

    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private final ServiceClientAuthenticator clientAuthenticator;
    private final ServiceTokenService serviceTokenService;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ServiceTokenResponse> issueToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(name = "grant_type", required = false) String grantType,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "client_secret", required = false) String clientSecret,
            @RequestParam(name = "scope", required = false) String scope,
            @RequestParam(name = "audience", required = false) String audience) {

        if (!CLIENT_CREDENTIALS.equals(grantType)) {
            throw OAuth2Exception.unsupportedGrantType();
        }
        ServiceClient client = clientAuthenticator.authenticate(authorization, clientId, clientSecret);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(serviceTokenService.issueToken(client, scope, audience));
    }
}
