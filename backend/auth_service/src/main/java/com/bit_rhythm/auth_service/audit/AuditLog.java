package com.bit_rhythm.auth_service.audit;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Id;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(schema = "auth_service", name = "audit_log")
@Setter
@Getter
public class AuditLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", updatable = false)
    private UUID actorId;

    @Column(name = "target_user_id", updatable = false)
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 60, updatable = false)
    private AuditAction action;

    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 512, updatable = false)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> details;

    public static AuditLog from(AuditEvent event) {
        AuditLog log = new AuditLog();
        log.actorId = event.actorId();
        log.targetUserId = event.targetUserId();
        log.action = event.action();
        log.ipAddress = truncate(event.ipAddress(), 45);
        log.userAgent = truncate(event.userAgent(), 512);
        log.details = (event.details() == null || event.details().isEmpty()) ? null : event.details();
//        log.createdAt = event.occurredAt();
        return log;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
