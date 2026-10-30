package com.bit_rhythm.auth_service.auth;

import com.bit_rhythm.auth_service.auth.dto.request.ChangePasswordRequest;
import com.bit_rhythm.auth_service.auth.dto.request.ForgotPasswordRequest;
import com.bit_rhythm.auth_service.auth.dto.request.LoginRequest;
import com.bit_rhythm.auth_service.auth.dto.request.RefreshTokenRequest;
import com.bit_rhythm.auth_service.auth.dto.request.RegisterRequest;
import com.bit_rhythm.auth_service.auth.dto.request.ResetPasswordRequest;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.bit_rhythm.auth_service.security.AuthUser;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ResponseFactory responseFactory;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseFormat<Object>> register(
            @Valid @RequestBody RegisterRequest request) {
        return responseFactory.response(AuthCode.SUCCESS,
                authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseFormat<Object>> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return responseFactory.response(AuthCode.SUCCESS,
                authService.login(request, servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseFormat<Object>> refresh(
            @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest servletRequest) {
        return responseFactory.response(AuthCode.SUCCESS,
                authService.refresh(request, servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent")));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseFormat<Object>> me(
            @AuthenticationPrincipal AuthUser user) {
        return responseFactory.response(AuthCode.SUCCESS, authService.me(user.id()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponseFormat<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AuthUser user)
    {
        authService.changePassword(request, user.id());
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponseFormat<Object>> requestPasswordReset(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.resetPassword(request.email());
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseFormat<Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseFormat<Object>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponseFormat<Object>> logoutAll(
            @AuthenticationPrincipal AuthUser user
    ) {
        authService.logoutAllSessions(user.id());
        return responseFactory.response(AuthCode.SUCCESS, null);
    }
}
