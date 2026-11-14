package com.bit_rhythm.auth_service.rbac.dto.response;

import com.bit_rhythm.auth_service.rbac.Permission;

public record PermissionResponse(
        Long id,
        String name,
        Long priority,
        String description,
        String createdAt,
        String updatedAt
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getPriority(),
                permission.getDescription(),
                permission.getCreatedAt().toString(),
                permission.getUpdatedAt().toString()
        );
    }

    public PermissionResponse(Permission permission) {
        this(
                permission.getId(),
                permission.getName(),
                permission.getPriority(),
                permission.getDescription(),
                permission.getCreatedAt().toString(),
                permission.getUpdatedAt().toString()
        );
    }
}
