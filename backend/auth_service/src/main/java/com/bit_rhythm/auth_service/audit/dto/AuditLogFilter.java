package com.bit_rhythm.auth_service.audit.dto;

import com.bit_rhythm.auth_service.audit.AuditAction;

import java.time.Instant;
import java.util.UUID;

/** All fields optional. {@code from} and {@code to} are both inclusive. */
public record AuditLogFilter(
        UUID targetUserId,
        UUID actorId,
        AuditAction action,
        Instant from,
        Instant to) {
}
