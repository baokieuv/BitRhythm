package com.bit_rhythm.auth_service.admin.dto;

import com.bit_rhythm.auth_service.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String username,
        String email,
        String status,
        boolean emailVerified,
        boolean mfaEnabled,
        Integer failedLoginAttempts,
        LocalDateTime lockedUntil,
        LocalDateTime passwordChangedAt,
        LocalDateTime lastLoginAt,
        List<String> roles) {

    public AdminUserResponse(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                Boolean.TRUE.equals(user.getEmailVerified()),
                Boolean.TRUE.equals(user.getMfaEnabled()),
                user.getFailedLoginAttempts(),
                user.getLockedUntil(),
                user.getPasswordChangedAt(),
                user.getLastLoginAt(),
                user.getRoles().stream().map(role -> role.getName()).sorted().toList());
    }
}
