package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.enums.RevokeReason;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.exception.AuthException;
import com.bit_rhythm.auth_service.common.util.CryptoService;
import com.bit_rhythm.auth_service.common.util.TokenGenerator;
import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int MAX_USER_AGENT_LENGTH = 512;

    private final RefreshTokenRepository refreshTokenRepository;
    private final CryptoService cryptoService;
    private final JwtProperties jwtProperties;

    @Transactional
    public IssuedToken issue(User user, String ipAddress, String userAgent) {
        return issue(user.getId(), TokenGenerator.generateFamilyId(), ipAddress, userAgent);
    }

    @Transactional
    public IssuedToken issue(Long userId, long familyId, String ipAddress, String userAgent) {
        String rawToken = TokenGenerator.generateRefreshToken();
        LocalDateTime now = LocalDateTime.now();
        RefreshToken entity = RefreshToken.builder().
                userId(userId).
                familyId(familyId).
                tokenHash(cryptoService.hashToken(rawToken)).
                issuedAt(now).
                expiresAt(now.plus(jwtProperties.refreshTokenTtl())).
                ipAddress(ipAddress).
                userAgent(truncate(userAgent, MAX_USER_AGENT_LENGTH)).
                build();

        refreshTokenRepository.save(entity);
        return new IssuedToken(rawToken, entity);
    }

    /**
     * Rotates a refresh token. noRollbackFor: the revocations done before throwing
     * (reuse detection, expiry) must be committed, otherwise they are silently rolled back.
     */
    @Transactional(noRollbackFor = AuthException.class)
    public IssuedToken rotate(String rawToken, String ipAddress, String userAgent) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }
        String hash = cryptoService.hashToken(rawToken);
        RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(hash)
                .orElseThrow(() -> new AuthException(AuthCode.INVALID_REFRESH_TOKEN));
        LocalDateTime now = LocalDateTime.now();
        if (current.getRevokedAt() != null) {
            refreshTokenRepository.revokeFamily(current.getFamilyId(), RevokeReason.TOKEN_REUSE.name(), now);
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }
        if (!current.getExpiresAt().isAfter(now)) {
            markRevoked(current, RevokeReason.EXPIRED, now);
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }

        markRevoked(current, RevokeReason.ROTATED, now);
        return issue(current.getUserId(), current.getFamilyId(), ipAddress, userAgent);
    }

    @Transactional
    public void revoke(String rawToken, RevokeReason reason) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(cryptoService.hashToken(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                markRevoked(token, reason, LocalDateTime.now());
            }
        });
    }

    @Transactional
    public void revokeAll(Long userId, RevokeReason reason) {
        refreshTokenRepository.revokeAllByUser(userId, reason.name(), LocalDateTime.now());
    }

    /** Deletes tokens that expired more than {@code retention} ago. */
    @Transactional
    public int purgeExpired(Duration retention) {
        return refreshTokenRepository.deleteExpiredBefore(LocalDateTime.now().minus(retention));
    }

    private String truncate(String value, int maxLength) {
        return value == null ? null : value.substring(0, Math.min(value.length(), maxLength));
    }

    private void markRevoked(RefreshToken token, RevokeReason reason, LocalDateTime at) {
        token.setRevokedAt(at);
        token.setRevokedReason(reason.name());
        refreshTokenRepository.save(token);
    }

    public record IssuedToken(String rawToken, RefreshToken entity, UserReference reference) {
        public IssuedToken(String rawToken, RefreshToken entity) {
            this(rawToken, entity, null);
        }
    }

    public record UserReference(Long userId, Long familyId) {
    }
}
