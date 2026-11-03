package com.bit_rhythm.auth_service.auth;

import com.bit_rhythm.auth_service.auth.dto.request.ChangePasswordRequest;
import com.bit_rhythm.auth_service.auth.dto.request.LoginRequest;
import com.bit_rhythm.auth_service.auth.dto.request.RefreshTokenRequest;
import com.bit_rhythm.auth_service.auth.dto.request.RegisterRequest;
import com.bit_rhythm.auth_service.auth.dto.request.ResetPasswordRequest;
import com.bit_rhythm.auth_service.auth.dto.response.TokenResponse;
import com.bit_rhythm.auth_service.auth.dto.response.UserResponse;
import com.bit_rhythm.auth_service.common.enums.Purpose;
import com.bit_rhythm.auth_service.common.enums.RevokeReason;
import com.bit_rhythm.auth_service.common.enums.UserStatus;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.exception.AuthException;
import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.token.JwtService;
import com.bit_rhythm.auth_service.token.RefreshTokenService;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.user.UserService;
import com.bit_rhythm.auth_service.verification.VerificationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final VerificationService verificationService;

    /** Valid hash generated with the real encoder, so unknown-user logins cost the same as real ones. */
    private String dummyPasswordHash;

    @PostConstruct
    void initDummyPasswordHash() {
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing-equalization");
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    @Transactional
    public UserResponse register(RegisterRequest request) {
        User user = userService.register(request);
        verificationService.issueToken(user, Purpose.EMAIL_VERIFICATION);
        return new UserResponse(user);
    }

    // ------------------------------------------------------------------
    // Authentication / session
    // ------------------------------------------------------------------

    // noRollbackFor: failed-attempt counters must be persisted even though we throw.
    @Transactional(noRollbackFor = AuthException.class)
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userService.findOptionalByIdentifier(request.identifier()).orElse(null);
        boolean passwordMatches = passwordEncoder.matches(
                request.password(), user == null ? dummyPasswordHash : user.getPasswordHash());

        if (user == null || !passwordMatches) {
            if (user != null) {
                loginAttemptService.recordFailure(user);
            }
            throw new AuthException(AuthCode.INVALID_CREDENTIALS);
        }

        ensureAccountCanAuthenticate(user);
        loginAttemptService.recordSuccess(user);
        return issueTokens(user, ipAddress, userAgent);
    }

    @Transactional(noRollbackFor = AuthException.class)
    public TokenResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshTokenService.IssuedToken rotated =
                refreshTokenService.rotate(request.refreshToken(), ipAddress, userAgent);
        User user = userService.findById(rotated.reference().userId());

        if (isDisabled(user) || !isEmailVerified(user)) {
            refreshTokenService.revokeAll(user.getId(), RevokeReason.ACCOUNT_STATE_INVALID);
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }
        return toTokenResponse(user, rotated.rawToken());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken(), RevokeReason.LOGOUT);
    }

    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenService.revokeAll(userId, RevokeReason.LOGOUT_ALL);
    }

    // ------------------------------------------------------------------
    // Profile
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        return new UserResponse(userService.findById(userId));
    }

    // ------------------------------------------------------------------
    // Password management
    // ------------------------------------------------------------------

    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {
        User user = userService.findById(userId);
        ensureAccountCanAuthenticate(user);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new AuthException(AuthCode.INVALID_CREDENTIALS);
        }

        userService.updatePassword(user, request.newPassword());
        refreshTokenService.revokeAll(userId, RevokeReason.PASSWORD_CHANGED);
    }

    /**
     * Step 1 of password recovery: send a reset token by email.
     * Silent when the account does not exist or is disabled (prevents account enumeration).
     */
    @Transactional
    public void requestPasswordReset(String email) {
        userService.findOptionalByIdentifier(email)
                .filter(user -> !isDisabled(user))
                .ifPresent(user -> verificationService.issueToken(user, Purpose.PASSWORD_RESET));
    }

    /** Step 2 of password recovery: consume the token and set the new password. */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = verificationService.consumePasswordReset(request.token());
        if (isDisabled(user)) {
            throw new AuthException(AuthCode.ACCOUNT_DISABLED);
        }
        userService.updatePassword(user, request.newPassword());
        refreshTokenService.revokeAll(user.getId(), RevokeReason.PASSWORD_CHANGED);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void ensureAccountCanAuthenticate(User user) {
        if (loginAttemptService.isLocked(user)) {
            throw new AuthException(AuthCode.ACCOUNT_LOCKED);
        }
        if (isDisabled(user)) {
            throw new AuthException(AuthCode.ACCOUNT_DISABLED);
        }
        if (!isEmailVerified(user)) {
            throw new AuthException(AuthCode.EMAIL_NOT_VERIFIED);
        }
    }

    private boolean isDisabled(User user) {
        return UserStatus.DISABLED.name().equals(user.getStatus());
    }

    private boolean isEmailVerified(User user) {
        return Boolean.TRUE.equals(user.getEmailVerified());
    }

    private TokenResponse issueTokens(User user, String ipAddress, String userAgent) {
        RefreshTokenService.IssuedToken refresh =
                refreshTokenService.issue(user, ipAddress, userAgent);
        return toTokenResponse(user, refresh.rawToken());
    }

    private TokenResponse toTokenResponse(User user, String rawRefreshToken) {
        return new TokenResponse(
                jwtService.generateAccessToken(user),
                rawRefreshToken,
                TOKEN_TYPE,
                durationSeconds(jwtProperties.accessTokenTtl()));
    }

    private long durationSeconds(Duration duration) {
        return Math.max(1, duration.toSeconds());
    }
}