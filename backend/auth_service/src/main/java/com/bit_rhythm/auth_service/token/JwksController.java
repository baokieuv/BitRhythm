package com.bit_rhythm.auth_service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * Public endpoint: other services (and the gateway) download the public keys here to verify tokens
 * locally. Must be permitAll in the security config. The body is the raw JWK Set, not wrapped in ApiResponseFormat.
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final SigningKeyService signingKeyService;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(signingKeyService.jwks());
    }
}
