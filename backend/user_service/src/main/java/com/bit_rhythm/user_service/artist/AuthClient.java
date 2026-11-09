package com.bit_rhythm.user_service.artist;

import org.springframework.stereotype.Component;

/**
 * [S2] grantRole(userId, "ARTIST"): POST {auth}/internal/users/{id}/roles bằng service token
 * (ServiceTokenProvider của core, audience auth-service, scope roles:write).
 */
@Component
public class AuthClient {
    // TODO [S2]
}
