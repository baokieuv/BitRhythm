package com.bit_rhythm.auth_service.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEvent(
        AuditAction action,
        UUID actorId,
        UUID targetUserId,
        String ipAddress,
        String userAgent,
        Map<String, Object> details,
        Instant occurredAt
) {
}
