package com.bit_rhythm.auth_service.verification;

import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.exception.AuthException;
import com.bit_rhythm.auth_service.common.util.CryptoService;
import com.bit_rhythm.auth_service.common.util.TokenGenerator;
import com.bit_rhythm.auth_service.config.AppProperties;
import com.bit_rhythm.auth_service.event.UserEventPublisher;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.user.UserRepository;
import com.bit_rhythm.auth_service.user.UserService;
import com.bit_rhythm.auth_service.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationService {

    public enum Purpose {
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }

    private final OneTimeTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final CryptoService cryptoService;
    private final AppProperties appProperties;
    private final UserEventPublisher userEventPublisher;
    private final UserService userService;

    /**
     * Creates and sends an email-verification token.
     */
    @Transactional
    public void issue(User user) {
        issue(user, Purpose.EMAIL_VERIFICATION);
    }

    /**
     * Creates and sends a token for the requested verification purpose.
     * Only the token hash is persisted; the raw token is sent to the user.
     */
    @Transactional
    public void issue(User user, Purpose purpose) {
        if (user == null || user.getId() == null || purpose == null) {
            throw new IllegalArgumentException("User and verification purpose are required");
        }

        String rawToken = TokenGenerator.generateRefreshToken();
        LocalDateTime now = LocalDateTime.now();
        OneTimeToken token = OneTimeToken.builder()
                .userId(user.getId())
                .type(purpose.name())
                .tokenHash(cryptoService.hashToken(rawToken))
                .expiresAt(now.plus(appProperties.verificationTokenTtl()))
                .usedAt(null)
                .build();

        tokenRepository.save(token);
        userEventPublisher.publishVerificationNotification(user, rawToken, purpose);
    }

    @Transactional
    public void verify(String rawToken) {
        OneTimeToken token = consume(rawToken, Purpose.EMAIL_VERIFICATION);
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new AuthException(AuthCode.INVALID_REFRESH_TOKEN));

        user.setEmailVerified(true);
        if (!UserStatus.DISABLED.name().equals(user.getStatus())) {
            user.setStatus(UserStatus.ACTIVE.name());
        }
        userRepository.save(user);
    }

    @Transactional
    public void resend(String email) {
        userService.findOptionalByEmail(email)
                .filter(user -> !Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(user -> issue(user, Purpose.EMAIL_VERIFICATION));
    }

    /**
     * Requests a reset without revealing whether the email exists.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        userService.findOptionalByEmail(email)
                .ifPresent(user -> issue(user, Purpose.PASSWORD_RESET));
    }

    @Transactional
    public User consumePasswordReset(String rawToken) {
        OneTimeToken token = consume(rawToken, Purpose.PASSWORD_RESET);
        return userRepository.findById(token.getUserId())
                .orElseThrow(() -> new AuthException(AuthCode.INVALID_REFRESH_TOKEN));
    }

    private OneTimeToken consume(String rawToken, Purpose purpose) {
        if (rawToken == null || rawToken.isBlank() || purpose == null) {
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }

        OneTimeToken token = tokenRepository.findByTokenHashAndType(
                        cryptoService.hashToken(rawToken), purpose.name())
                .orElseThrow(() -> new AuthException(AuthCode.INVALID_REFRESH_TOKEN));

        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }

        token.setUsedAt(LocalDateTime.now());
        return tokenRepository.save(token);
    }
}
