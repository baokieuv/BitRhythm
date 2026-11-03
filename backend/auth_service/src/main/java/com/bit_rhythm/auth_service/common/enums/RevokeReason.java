package com.bit_rhythm.auth_service.common.enums;

/** Why a refresh token was revoked. Stored as the enum name in refresh_tokens.revoked_reason. */
public enum RevokeReason {
    LOGOUT,
    LOGOUT_ALL,
    PASSWORD_CHANGED,
    PASSWORD_RESET,
    EMAIL_VERIFICATION_RESEND,
    ACCOUNT_STATE_INVALID,
    TOKEN_REUSE,
    ROTATED,
    EXPIRED
}