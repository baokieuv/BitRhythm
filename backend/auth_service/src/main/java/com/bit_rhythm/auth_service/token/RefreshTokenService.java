package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.exception.AuthException;
import com.bit_rhythm.auth_service.common.util.CryptoService;
import com.bit_rhythm.auth_service.common.util.TokenGenerator;
import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final CryptoService cryptoService;
    private final JwtProperties jwtProperties;

    @Transactional
    public IssuedToken issue(User user, String ipAddress, String userAgent) {
        return issue(user, TokenGenerator.generateFamilyId(), ipAddress, userAgent);
    }

    @Transactional
    public IssuedToken issue(User user, long familyId, String ipAddress, String userAgent) {
        String rawToken = TokenGenerator.generateRefreshToken();
        LocalDateTime now = LocalDateTime.now();
        RefreshToken entity = RefreshToken.builder().
                userId(user.getId()).
                familyId(familyId).
                tokenHash(cryptoService.hashToken(rawToken)).
                issuedAt(now).
                expiresAt(now.plus(jwtProperties.refreshTokenTtl())).
                ipAddress(ipAddress).
                userAgent(truncate(userAgent, 512)).
                build();

        refreshTokenRepository.save(entity);
        return new IssuedToken(rawToken, entity);
    }

    @Transactional
    public IssuedToken rotate(String rawToken, String ipAddress, String userAgent) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }
        String hash = cryptoService.hashToken(rawToken);
        RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(hash)
                .orElseThrow(() -> new AuthException(AuthCode.INVALID_REFRESH_TOKEN));
        LocalDateTime now = LocalDateTime.now();
        if (current.getRevokedAt() != null) {
            refreshTokenRepository.revokeFamily(current.getFamilyId(), "REFRESH_TOKEN_REUSE");
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }
        if (!current.getExpiresAt().isAfter(now)) {
            current.setRevokedAt(now);
            current.setRevokedReason("EXPIRED");
            refreshTokenRepository.save(current);
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }

        current.setRevokedAt(now);
        current.setRevokedReason("ROTATED");
        refreshTokenRepository.save(current);
        UserReference reference = new UserReference(current.getUserId(), current.getFamilyId());
        IssuedToken replacement = issueReference(reference, ipAddress, userAgent);
        return new IssuedToken(replacement.rawToken(), replacement.entity(), reference);
    }

    @Transactional
    public void revoke(String rawToken, String reason) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(cryptoService.hashToken(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(LocalDateTime.now());
                token.setRevokedReason(reason);
                refreshTokenRepository.save(token);
            }
        });
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.revokeAllByUser(userId);
    }

    private IssuedToken issueReference(UserReference reference, String ipAddress, String userAgent) {
        String rawToken = TokenGenerator.generateRefreshToken();
        LocalDateTime now = LocalDateTime.now();
        RefreshToken entity = RefreshToken.builder().
                userId(reference.userId()).
                familyId(reference.familyId()).
                tokenHash(cryptoService.hashToken(rawToken)).
                issuedAt(now).
                expiresAt(now.plus(jwtProperties.refreshTokenTtl())).
                ipAddress(ipAddress).
                userAgent(truncate(userAgent, 512)).
                build();

        refreshTokenRepository.save(entity);
        return new IssuedToken(rawToken, entity);
    }

    private String truncate(String value, int maxLength) {
        return value == null ? null : value.substring(0, Math.min(value.length(), maxLength));
    }

    public record IssuedToken(String rawToken, RefreshToken entity, UserReference reference) {
        public IssuedToken(String rawToken, RefreshToken entity) {
            this(rawToken, entity, null);
        }
    }

    public record UserReference(Long userId, Long familyId) {
    }
}
