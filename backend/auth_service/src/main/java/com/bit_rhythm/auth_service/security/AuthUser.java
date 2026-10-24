package com.bit_rhythm.auth_service.security;

import java.util.List;
import java.util.UUID;

/** The authenticated principal built by JwtAuthFilter from a valid access token. */
public record AuthUser(UUID id, String email, List<String> role) {
}