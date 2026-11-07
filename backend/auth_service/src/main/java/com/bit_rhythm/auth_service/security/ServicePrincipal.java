package com.bit_rhythm.auth_service.security;

import java.util.List;

/** Principal for requests authenticated with a service token (machine-to-machine). */
public record ServicePrincipal(String clientId, List<String> scopes) {
}