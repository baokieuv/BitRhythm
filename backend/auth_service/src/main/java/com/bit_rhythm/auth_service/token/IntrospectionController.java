package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.exception.OAuth2Exception;
import com.bit_rhythm.auth_service.token.dto.response.TokenIntrospectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RFC 7662 token introspection. The caller must authenticate as a registered service client
 * (Basic or client_id/client_secret) and always gets 200; an invalid token is {"active": false}.
 * Must be permitAll in the security config (client auth is done here).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/token/introspect")
public class IntrospectionController {

    private final ServiceClientAuthenticator clientAuthenticator;
    private final TokenIntrospectionService introspectionService;

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenIntrospectionResponse> introspect(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "client_secret", required = false) String clientSecret,
            @RequestParam(name = "token", required = false) String token) {

        clientAuthenticator.authenticate(authorization, clientId, clientSecret);
        if (token == null || token.isBlank()) {
            throw OAuth2Exception.invalidRequest("token is required");
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(introspectionService.introspect(token));
    }
}
