package com.bit_rhythm.auth_service.auth;

import com.bit_rhythm.auth_service.auth.dto.request.ChangePasswordRequest;
import com.bit_rhythm.auth_service.auth.dto.request.LoginRequest;
import com.bit_rhythm.auth_service.auth.dto.request.RefreshTokenRequest;
import com.bit_rhythm.auth_service.auth.dto.request.RegisterRequest;
import com.bit_rhythm.auth_service.auth.dto.request.ResetPasswordRequest;
import com.bit_rhythm.auth_service.auth.dto.response.TokenResponse;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.exception.AuthException;
import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.token.JwtService;
import com.bit_rhythm.auth_service.token.RefreshTokenService;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.user.UserService;
import com.bit_rhythm.auth_service.user.UserStatus;
import com.bit_rhythm.auth_service.verification.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$12$C6UzMDM.H6dfI/f/IKcEe.8Jq9g0Qv8mJf5w7fW8j5xQfM2cB5u";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final VerificationService verificationService;

    @Transactional
    public User register(RegisterRequest request) {
        User user = userService.register(request);
        verificationService.issue(user);
        return user;
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userService.findOptionalByIdentifier(request.identifier()).orElse(null);
        boolean passwordMatches = passwordEncoder.matches(
                request.password(), user == null ? DUMMY_PASSWORD_HASH : user.getPasswordHash());
        if (user == null || !passwordMatches) {
            if (user != null) {
                loginAttemptService.recordFailure(user);
            }
            throw new AuthException(AuthCode.INVALID_CREDENTIALS);
        }
        if (loginAttemptService.isLocked(user)) {
            throw new AuthException(AuthCode.ACCOUNT_LOCKED);
        }
        if (UserStatus.DISABLED.name().equals(user.getStatus())) {
            throw new AuthException(AuthCode.ACCOUNT_DISABLED);
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AuthException(AuthCode.EMAIL_NOT_VERIFIED);
        }

        loginAttemptService.recordSuccess(user);
        return createTokenResponse(user, ipAddress, userAgent);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshTokenService.IssuedToken rotated =
                refreshTokenService.rotate(request.refreshToken(), ipAddress, userAgent);
        User user = userService.findById(rotated.reference().userId());
        if (UserStatus.DISABLED.name().equals(user.getStatus())
                || !Boolean.TRUE.equals(user.getEmailVerified())) {
            refreshTokenService.revokeAll(user.getId());
            throw new AuthException(AuthCode.INVALID_REFRESH_TOKEN);
        }
        return new TokenResponse(
                jwtService.generateAccessToken(user),
                rotated.rawToken(),
                "Bearer",
                durationSeconds(jwtProperties.accessTokenTtl()));
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken(), "LOGOUT");
    }

    @Transactional
    public void logoutAllSessions(Long userId) {
        refreshTokenService.revokeAll(userId);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {
        User user = userService.findById(userId);
        if (user == null || !passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new AuthException(AuthCode.INVALID_CREDENTIALS);
        }

        if (loginAttemptService.isLocked(user)) {
            throw new AuthException(AuthCode.ACCOUNT_LOCKED);
        }
        if (UserStatus.DISABLED.name().equals(user.getStatus())) {
            throw new AuthException(AuthCode.ACCOUNT_DISABLED);
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AuthException(AuthCode.EMAIL_NOT_VERIFIED);
        }

        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        userService.updatePassword(user, newPasswordHash);
        refreshTokenService.revokeAll(userId);
    }

    @Transactional
    public void resetPassword(String email) {
        verificationService.requestPasswordReset(email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = verificationService.consumePasswordReset(request.token());
        if (UserStatus.DISABLED.name().equals(user.getStatus())) {
            throw new AuthException(AuthCode.ACCOUNT_DISABLED);
        }
        userService.updatePassword(user, request.newPassword());
        refreshTokenService.revokeAll(user.getId());
    }

    private TokenResponse createTokenResponse(User user, String ipAddress, String userAgent) {
        RefreshTokenService.IssuedToken refresh =
                refreshTokenService.issue(user, ipAddress, userAgent);
        return new TokenResponse(
                jwtService.generateAccessToken(user),
                refresh.rawToken(),
                "Bearer",
                durationSeconds(jwtProperties.accessTokenTtl()));
    }

    private long durationSeconds(Duration duration) {
        return Math.max(1, duration.toSeconds());
    }

    public record RegisteredUser(Long id, String username, String email) {
        public RegisteredUser(User user) {
            this(user.getId(), user.getUsername(), user.getEmail());
        }
    }
}
