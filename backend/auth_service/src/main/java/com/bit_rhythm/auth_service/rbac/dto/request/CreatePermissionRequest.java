package com.bit_rhythm.auth_service.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionRequest(
        @NotBlank(message = "Permission name is required")
        String name,

        String description
) {
}
