package com.bit_rhythm.auth_service.token;

/** Claim names and token types shared by every token this service issues. */
public final class TokenClaims {

    public static final String TOKEN_TYPE = "token_type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_SERVICE = "service";

    public static final String CLIENT_ID = "client_id";
    public static final String SCOPE = "scopes";
    public static final String EMAIL = "email";
    public static final String ROLES = "roles";
    public static final String PERMISSIONS = "permissions";

    private TokenClaims() {
    }
}
