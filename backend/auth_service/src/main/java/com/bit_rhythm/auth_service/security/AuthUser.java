package com.bit_rhythm.auth_service.security;

import java.util.List;

/** The authenticated principal built by JwtAuthFilter from a valid access token. */
public record AuthUser(Long id, String email, List<String> roles, List<String> permissions) {
}