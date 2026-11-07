package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.exception.OAuth2Exception;
import com.bit_rhythm.core.common.CommonEnums;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ServiceClientAuthenticator {
    private static final String BASIC_PREFIX = "Basic ";

    private final ServiceClientRepository serviceClientRepository;
    private final PasswordEncoder passwordEncoder;

    /** Valid hash, so an unknown client_id costs as much time as a wrong secret (no timing side channel). */
    private String dummySecretHash;

    @PostConstruct
    void initDummySecretHash() {
        this.dummySecretHash = passwordEncoder.encode("dummy-client-secret-for-timing-equalization");
    }

    public ServiceClient authenticate(String authorizationHeader, String clientId, String clientSecret) {
        String[] credentials = extractCredentials(authorizationHeader, clientId, clientSecret);

        ServiceClient client = serviceClientRepository
                .findByClientIdAndIsActiveTrueAndIsDeleted(
                        credentials[0].trim(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElse(null);

        boolean secretMatches = passwordEncoder.matches(
                credentials[1], client == null ? dummySecretHash : client.getClientSecretHash());
        if (client == null || !secretMatches) {
            throw OAuth2Exception.invalidClient();
        }
        return client;
    }

    private String[] extractCredentials(String authorizationHeader, String clientId, String clientSecret) {
        if (authorizationHeader != null && authorizationHeader.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            try {
                String decoded = new String(
                        Base64.getDecoder().decode(authorizationHeader.substring(BASIC_PREFIX.length()).trim()),
                        StandardCharsets.UTF_8
                );

                int separator = decoded.indexOf(':');
                if (separator <= 0) {
                    throw OAuth2Exception.invalidClient();
                }
                // RFC 6749 §2.3.1: id and secret are form-urlencoded before being Base64-ed.
                return new String[]{
                        URLDecoder.decode(decoded.substring(0, separator), StandardCharsets.UTF_8),
                        URLDecoder.decode(decoded.substring(separator + 1), StandardCharsets.UTF_8)};
            } catch (IllegalArgumentException e) {
                // Ignore base64 decode errors
                throw OAuth2Exception.invalidClient();
            }
        }
        if (hasText(clientId) && hasText(clientSecret)) {
            return new String[]{clientId, clientSecret};
        }
        throw OAuth2Exception.invalidClient();
    }
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
