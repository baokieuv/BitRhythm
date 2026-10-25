package com.bit_rhythm.auth_service.rbac.dto.request;

public record UpdatePermissionRequest(
        String name,
        String description
) {
}
