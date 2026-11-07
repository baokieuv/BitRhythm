package com.bit_rhythm.auth_service.token.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** RFC 7662 introspection response. Epoch seconds for iat/exp. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenIntrospectionResponse(
        boolean active,
        String scope,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("token_type") String tokenType,
        String sub,
        List<String> aud,
        String iss,
        Long iat,
        Long exp,
        String jti
) {
    public static TokenIntrospectionResponse inactive() {
        return new TokenIntrospectionResponse(false, null, null, null, null, null, null, null, null, null);
    }
}
