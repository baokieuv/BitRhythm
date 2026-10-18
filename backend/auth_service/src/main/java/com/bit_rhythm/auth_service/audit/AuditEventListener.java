package com.bit_rhythm.auth_service.audit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Persists audit events on a dedicated thread pool. Runs outside the business transaction,
 * so a failed or rolled-back request (e.g. a failed login) is still recorded, and an audit
 * failure can never break the business operation.
 *
 * Every event is also written to the dedicated "AUDIT" logger (without details), so it can be
 * shipped to your log pipeline and acts as a fallback if the database write fails.
 */
@Component
public class AuditEventListener {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");
    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditLogRepository repository;

    public AuditEventListener(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Async("auditExecutor")
    @EventListener
    public void handle(AuditEvent event) {
        AUDIT.info("action={} actor={} target={} ip={}",
                event.action(), event.actorId(), event.targetUserId(), event.ipAddress());
        try {
            repository.save(AuditLog.from(event));
        } catch (Exception ex) {
            log.error("Failed to persist audit event action={} actor={} target={}",
                    event.action(), event.actorId(), event.targetUserId(), ex);
        }
    }
}

