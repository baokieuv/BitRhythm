package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.token.dto.response.TokenIntrospectionResponse;
import com.bit_rhythm.core.common.CommonEnums;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Online validity check for tokens we issued. Unlike local JWKS verification it also notices that a
 * service client was disabled after its token was issued, so use it for sensitive operations only.
 */
@Service
@RequiredArgsConstructor
public class TokenIntrospectionService {
    private final JwtService jwtService;
    private final ServiceClientRepository serviceClientRepository;

    public TokenIntrospectionResponse introspect(String token) {
        Claims claims;
        try {
            claims = jwtService.verify(token.trim());
        } catch (Exception e) {
            return TokenIntrospectionResponse.inactive();
        }

        String type = claims.get(TokenClaims.TOKEN_TYPE, String.class);
        String clientId = claims.get(TokenClaims.CLIENT_ID, String.class);

        if (TokenClaims.TYPE_SERVICE.equals(type)) {
            if (clientId == null || !isClientActive(clientId)) {
                return TokenIntrospectionResponse.inactive();
            }
        } else if (!TokenClaims.TYPE_ACCESS.equals(type)) {
            return TokenIntrospectionResponse.inactive();
        }

        return new TokenIntrospectionResponse(
                true,
                claims.get(TokenClaims.SCOPE, String.class),
                clientId,
                type,
                claims.getSubject(),
                claims.getAudience() == null ? null : new ArrayList<>(claims.getAudience()),
                claims.getIssuer(),
                claims.getIssuedAt() == null ? null : claims.getIssuedAt().toInstant().getEpochSecond(),
                claims.getExpiration() == null ? null : claims.getExpiration().toInstant().getEpochSecond(),
                claims.getId());
    }

    private boolean isClientActive(String clientId) {
        return serviceClientRepository
                .findByClientIdAndIsActiveTrueAndIsDeleted(clientId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .isPresent();
    }
}
