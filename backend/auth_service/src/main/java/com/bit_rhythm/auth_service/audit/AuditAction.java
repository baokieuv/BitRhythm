package com.bit_rhythm.auth_service.audit;

/**
 * Every auditable event. Stored as a string (VARCHAR(60)) in audit_logs.action,
 * so adding a value never needs a migration. Never rename or remove existing values.
 */
public enum AuditAction {
    // Authentication
    REGISTERED,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    ACCOUNT_AUTO_LOCKED,
    LOGOUT,
    LOGOUT_ALL,
    REFRESH_TOKEN_REUSE_DETECTED,

    // Verification and recovery
    EMAIL_VERIFIED,
    VERIFICATION_EMAIL_RESENT,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_COMPLETED,
    PASSWORD_CHANGED,

    // MFA
    MFA_SETUP_STARTED,
    MFA_ENABLED,
    MFA_DISABLED,
    MFA_LOGIN_SUCCESS,
    MFA_LOGIN_FAILED,
    MFA_RECOVERY_CODE_USED,
    MFA_RECOVERY_CODES_REGENERATED,

    // RBAC
    ROLE_CREATED,
    ROLE_UPDATED,
    ROLE_DELETED,
    ROLE_ASSIGNED,
    ROLE_REMOVED,

    // Admin / internal
    USER_STATUS_CHANGED,
    USER_FORCE_LOGOUT,
    SERVICE_TOKEN_FAILED,
    SIGNING_KEY_ROTATED
}

