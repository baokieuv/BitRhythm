package com.bit_rhythm.auth_service.auth.dto.response;

import com.bit_rhythm.auth_service.rbac.Permission;
import com.bit_rhythm.auth_service.rbac.Role;
import com.bit_rhythm.auth_service.user.User;

import java.util.List;

public record UserResponse(
        Long id,
       String username,
       String email,
       String status,
       boolean emailVerified,
       boolean mfaEnabled,
       List<String> roles,
       List<String> permissions
) {
    public UserResponse(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                Boolean.TRUE.equals(user.getEmailVerified()),
                Boolean.TRUE.equals(user.getMfaEnabled()),
                user.getRoles().stream()
                        .map(Role::getName)
                        .distinct()
                        .sorted()
                        .toList(),
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getName)
                        .distinct()
                        .sorted()
                        .toList());
    }
}
