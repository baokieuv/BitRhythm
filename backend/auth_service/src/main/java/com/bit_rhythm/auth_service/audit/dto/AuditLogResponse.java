package com.bit_rhythm.auth_service.audit.dto;

import com.bit_rhythm.auth_service.audit.AuditAction;
import com.bit_rhythm.auth_service.audit.AuditLog;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        Long id,
        UUID actorId,
        UUID targetUserId,
        AuditAction action,
        String ipAddress,
        String userAgent,
        Map<String, Object> details,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorId(),
                log.getTargetUserId(),
                log.getAction(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getDetails(),
                log.getCreatedDate()
        );
    }
}
