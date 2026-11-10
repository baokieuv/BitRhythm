package com.bit_rhythm.user_service.profile;

public enum ProfileStatus {
    ACTIVE,
    HIDDEN,    // [S2] hidden by an admin: public lookups answer 404
    DELETED    // [S2] account deleted in auth-service: anonymized
}
