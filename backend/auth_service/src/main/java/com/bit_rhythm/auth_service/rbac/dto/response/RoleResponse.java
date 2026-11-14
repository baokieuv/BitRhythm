package com.bit_rhythm.auth_service.rbac.dto.response;

import com.bit_rhythm.auth_service.rbac.Role;

import java.util.List;

public record RoleResponse(
        Long id,
        String name,
        Long priority,
        String description,
        List<PermissionResponse> permissions
) {
    public RoleResponse(Role role) {
        this(
                role.getId(),
                role.getName(),
                role.getPriority(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(PermissionResponse::from)
                        .toList()
        );
    }
}
