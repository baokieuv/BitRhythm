package com.bit_rhythm.auth_service.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Returns the client IP. It deliberately does NOT parse X-Forwarded-For itself (trivially spoofable).
 * Set {@code server.forward-headers-strategy: framework} so Spring resolves the real client address
 * from the headers added by your trusted gateway/ingress, then {@code getRemoteAddr()} is correct.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        return request == null ? null : request.getRemoteAddr();
    }
}