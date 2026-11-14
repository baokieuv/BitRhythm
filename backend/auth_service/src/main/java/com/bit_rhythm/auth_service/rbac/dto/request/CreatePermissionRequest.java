package com.bit_rhythm.auth_service.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePermissionRequest(
        @NotBlank(message = "Permission name is required")
        String name,
        @NotNull(message = "Permission priority is required")
        Long priority,
        String description
) {
}
