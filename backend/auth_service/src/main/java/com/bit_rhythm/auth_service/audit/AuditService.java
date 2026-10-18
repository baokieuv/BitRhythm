package com.bit_rhythm.auth_service.audit;

import com.bit_rhythm.auth_service.common.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Entry point for recording audit events. Never throws and never blocks the caller:
 * it only captures context and publishes an {@link AuditEvent}; persistence happens
 * asynchronously in {@link AuditEventListener}.
 *
 * <ul>
 *   <li>{@code log(...)}: actor is taken from the current JWT (admin actions, authenticated calls).</li>
 *   <li>{@code logAs(...)}: pass the actor explicitly (login, register, reset-password, where the
 *       SecurityContext is empty).</li>
 * </ul>
 */
@Service
public class AuditService {

    private static final String REDACTED = "[REDACTED]";
    private static final List<String> SENSITIVE_FRAGMENTS =
            List.of("password", "token", "secret", "authorization", "recoverycode");

    private final ApplicationEventPublisher publisher;

    public AuditService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void log(AuditAction action, UUID targetUserId) {
        log(action, targetUserId, Map.of());
    }

    public void log(AuditAction action, UUID targetUserId, Map<String, Object> details) {
        Map<String, Object> safe = sanitize(details);
        UUID actorId = resolveActor(safe);
        publish(action, actorId, targetUserId, safe);
    }

    public void logAs(UUID actorId, AuditAction action, UUID targetUserId, Map<String, Object> details) {
        publish(action, actorId, targetUserId, sanitize(details));
    }

    /** "john.doe@example.com" becomes "j***@example.com". Use for failed logins with unknown users. */
    public static String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return "***";
        }
        if (at <= 1) {
            return "***" + email.substring(at);
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    // ------------------------------------------------------------------

    private void publish(AuditAction action, UUID actorId, UUID targetUserId, Map<String, Object> details) {
        HttpServletRequest request = currentRequest();
        String userAgent = request == null ? null : request.getHeader("User-Agent");
        publisher.publishEvent(new AuditEvent(
                action,
                actorId,
                targetUserId,
                ClientIpResolver.resolve(request),
                userAgent,
                details,
                Instant.now()));
    }

    /** Actor from the JWT subject. Service tokens have a non-UUID subject (client_id), which goes into details. */
    private UUID resolveActor(Map<String, Object> details) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String subject = jwtAuth.getToken().getSubject();
            if (subject == null) {
                return null;
            }

            try {
                return UUID.fromString(subject);
            } catch (IllegalArgumentException ex) {
                details.put("actorClientId", subject);
            }
        }
        return null;
    }

    private static HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    /** Defensive: never let secrets reach the audit table, even by mistake. Top-level keys only. */
    private static Map<String, Object> sanitize(Map<String, Object> details) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (details == null) {
            return result;
        }
        details.forEach((key, value) -> result.put(key, isSensitive(key) ? REDACTED : value));
        return result;
    }

    private static boolean isSensitive(String key) {
        String k = key.toLowerCase(Locale.ROOT);
        return k.equals("code") || SENSITIVE_FRAGMENTS.stream().anyMatch(k::contains);
    }
}
