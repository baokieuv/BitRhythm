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
import com.bit_rhythm.auth_service.common.enums.UserEventType;
import com.bit_rhythm.auth_service.common.enums.UserStatus;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.exception.AuthException;
import com.bit_rhythm.auth_service.config.JwtProperties;
import com.bit_rhythm.auth_service.event.UserEventPublisher;
import com.bit_rhythm.auth_service.token.JwtService;
import com.bit_rhythm.auth_service.token.RefreshToken;
import com.bit_rhythm.auth_service.token.RefreshTokenService;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.user.UserService;
import com.bit_rhythm.auth_service.verification.VerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private VerificationService verificationService;
    @Mock
    private UserEventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    private User user;
    private static final LoginRequest LOGIN = new LoginRequest("alice@example.com", "correct-password");
    private static final RefreshTokenRequest REFRESH = new RefreshTokenRequest("refresh-token");

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(42L)
                .username("alice")
                .email("alice@example.com")
                .passwordHash("stored-hash")
                .status(UserStatus.ACTIVE.name())
                .emailVerified(true)
                .mfaEnabled(false)
                .failedLoginAttempts(0)
                .version(0L)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        authService.initDummyPasswordHash();
    }

    @Test
    void registerIssuesVerificationAndPublishesRegistrationEvent() {
        RegisterRequest request = new RegisterRequest("alice", "alice@example.com", "a-very-secure-password");
        when(userService.register(request)).thenReturn(user);

        UserResponse response = authService.register(request);

        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
        verify(verificationService).issueToken(user, Purpose.EMAIL_VERIFICATION);
        verify(eventPublisher).publishUserEvent(user, UserEventType.USER_REGISTERED);
    }

    @Test
    void loginWithValidCredentialsRecordsSuccessAndReturnsTokens() {
        when(userService.findOptionalByIdentifier(LOGIN.identifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(LOGIN.password(), user.getPasswordHash())).thenReturn(true);
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofMinutes(10));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.issue(user, "127.0.0.1", "test-agent"))
                .thenReturn(issuedToken("refresh-token"));

        TokenResponse response = authService.login(LOGIN, "127.0.0.1", "test-agent");

        assertEquals(new TokenResponse("access-token", "refresh-token", "Bearer", 600), response);
        verify(loginAttemptService).recordSuccess(user);
        verify(refreshTokenService).issue(user, "127.0.0.1", "test-agent");
    }

    @Test
    void loginWithUnknownUserUsesDummyHashAndDoesNotIssueTokens() {
        when(userService.findOptionalByIdentifier("missing@example.com")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("missing@example.com", "wrong-password");
        when(passwordEncoder.matches("wrong-password", "dummy-hash")).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.login(request, "127.0.0.1", "agent"));

        assertEquals(AuthCode.INVALID_CREDENTIALS, exception.getErrorCode());
        verify(passwordEncoder).matches("wrong-password", "dummy-hash");
        verifyNoInteractions(loginAttemptService, refreshTokenService, jwtService);
    }

    @Test
    void loginWithWrongPasswordRecordsFailureAndRejects() {
        when(userService.findOptionalByIdentifier(LOGIN.identifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(LOGIN.password(), user.getPasswordHash())).thenReturn(false);

        AuthException exception = assertThrows(AuthException.class,
                () -> authService.login(LOGIN, "127.0.0.1", "agent"));

        assertEquals(AuthCode.INVALID_CREDENTIALS, exception.getErrorCode());
        verify(loginAttemptService).recordFailure(user);
        verifyNoInteractions(refreshTokenService, jwtService);
    }

    @Test
    void loginRejectsLockedDisabledAndUnverifiedAccounts() {
        when(userService.findOptionalByIdentifier(LOGIN.identifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(LOGIN.password(), user.getPasswordHash())).thenReturn(true);

        when(loginAttemptService.isLocked(user)).thenReturn(true);
        assertAuthCode(AuthCode.ACCOUNT_LOCKED, () -> authService.login(LOGIN, "ip", "agent"));

        when(loginAttemptService.isLocked(user)).thenReturn(false);
        user.setStatus(UserStatus.DISABLED.name());
        assertAuthCode(AuthCode.ACCOUNT_DISABLED, () -> authService.login(LOGIN, "ip", "agent"));

        user.setStatus(UserStatus.ACTIVE.name());
        user.setEmailVerified(false);
        assertAuthCode(AuthCode.EMAIL_NOT_VERIFIED, () -> authService.login(LOGIN, "ip", "agent"));

        verifyNoInteractions(refreshTokenService, jwtService);
    }

    @Test
    void refreshRotatesTokenAndReturnsAccessToken() {
        RefreshTokenService.IssuedToken rotated = issuedToken("rotated-refresh");
        when(refreshTokenService.rotate("refresh-token", "10.0.0.1", "agent")).thenReturn(rotated);
        when(userService.findById(42L)).thenReturn(user);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofSeconds(30));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");

        TokenResponse response = authService.refresh(REFRESH, "10.0.0.1", "agent");

        assertEquals(new TokenResponse("new-access", "rotated-refresh", "Bearer", 30), response);
        verify(refreshTokenService).rotate("refresh-token", "10.0.0.1", "agent");
        verify(refreshTokenService, never()).revokeAll(anyLong(), any());
    }

    @Test
    void refreshRejectsDisabledOrUnverifiedUserAndRevokesAllTokens() {
        RefreshTokenService.IssuedToken rotated = issuedToken("rotated-refresh");
        when(refreshTokenService.rotate(anyString(), anyString(), anyString())).thenReturn(rotated);
        when(userService.findById(42L)).thenReturn(user);
        user.setStatus(UserStatus.DISABLED.name());

        assertAuthCode(AuthCode.INVALID_REFRESH_TOKEN,
                () -> authService.refresh(REFRESH, "ip", "agent"));

        verify(refreshTokenService).revokeAll(42L, RevokeReason.ACCOUNT_STATE_INVALID);
        verifyNoInteractions(jwtService);
    }

    @Test
    void logoutRevokesOneTokenAndLogoutAllRevokesEveryToken() {
        authService.logout(REFRESH);
        authService.logoutAll(42L);

        verify(refreshTokenService).revoke("refresh-token", RevokeReason.LOGOUT);
        verify(refreshTokenService).revokeAll(42L, RevokeReason.LOGOUT_ALL);
    }

    @Test
    void getCurrentUserMapsTheCurrentUser() {
        when(userService.findById(42L)).thenReturn(user);

        UserResponse response = authService.getCurrentUser(42L);

        assertEquals(42L, response.id());
        assertEquals("alice", response.username());
        assertTrue(response.emailVerified());
    }

    @Test
    void changePasswordUpdatesPasswordAndRevokesSessions() {
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-very-secure-password");
        when(userService.findById(42L)).thenReturn(user);
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(passwordEncoder.matches("old-password", "stored-hash")).thenReturn(true);

        authService.changePassword(request, 42L);

        verify(userService).updatePassword(user, "new-very-secure-password");
        verify(refreshTokenService).revokeAll(42L, RevokeReason.PASSWORD_CHANGED);
    }

    @Test
    void changePasswordRejectsWrongOldPasswordWithoutChangingAnything() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-password", "new-very-secure-password");
        when(userService.findById(42L)).thenReturn(user);
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertAuthCode(AuthCode.INVALID_CREDENTIALS, () -> authService.changePassword(request, 42L));

        verify(userService, never()).updatePassword(any(), anyString());
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void requestPasswordResetIsSilentForUnknownAndDisabledUsers() {
        when(userService.findOptionalByIdentifier("missing@example.com")).thenReturn(Optional.empty());
        authService.requestPasswordReset("missing@example.com");

        user.setStatus(UserStatus.DISABLED.name());
        when(userService.findOptionalByIdentifier("alice@example.com")).thenReturn(Optional.of(user));
        authService.requestPasswordReset("alice@example.com");

        verifyNoInteractions(verificationService);
    }

    @Test
    void requestPasswordResetIssuesPasswordResetTokenForEnabledUser() {
        when(userService.findOptionalByIdentifier("alice@example.com")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("alice@example.com");

        verify(verificationService).issueToken(user, Purpose.PASSWORD_RESET);
    }

    @Test
    void resetPasswordConsumesTokenUpdatesPasswordAndRevokesSessions() {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "new-very-secure-password");
        when(verificationService.consumePasswordReset("reset-token")).thenReturn(user);

        authService.resetPassword(request);

        verify(userService).updatePassword(user, "new-very-secure-password");
        verify(refreshTokenService).revokeAll(42L, RevokeReason.PASSWORD_CHANGED);
    }

    @Test
    void resetPasswordRejectsDisabledUserWithoutChangingPassword() {
        user.setStatus(UserStatus.DISABLED.name());
        when(verificationService.consumePasswordReset("reset-token")).thenReturn(user);

        assertAuthCode(AuthCode.ACCOUNT_DISABLED,
                () -> authService.resetPassword(new ResetPasswordRequest("reset-token", "new-password")));

        verify(userService, never()).updatePassword(any(), anyString());
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void zeroOrSubsecondAccessTokenTtlStillProducesOneSecondExpiry() {
        when(userService.findOptionalByIdentifier(LOGIN.identifier())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(LOGIN.password(), user.getPasswordHash())).thenReturn(true);
        when(loginAttemptService.isLocked(user)).thenReturn(false);
        when(jwtProperties.accessTokenTtl()).thenReturn(Duration.ofMillis(999));
        when(jwtService.generateAccessToken(user)).thenReturn("access");
        when(refreshTokenService.issue(any(), anyString(), anyString())).thenReturn(issuedToken("refresh"));

        TokenResponse response = authService.login(LOGIN, "ip", "agent");

        assertEquals(1, response.expiresIn());
    }

    private RefreshTokenService.IssuedToken issuedToken(String rawToken) {
        RefreshToken entity = RefreshToken.builder().userId(42L).familyId(7L).build();
        return new RefreshTokenService.IssuedToken(rawToken, entity);
    }

    private void assertAuthCode(AuthCode expected, org.junit.jupiter.api.function.Executable action) {
        AuthException exception = assertThrows(AuthException.class, action);
        assertEquals(expected, exception.getErrorCode());
    }
}
