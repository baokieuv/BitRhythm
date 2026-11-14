package com.bit_rhythm.auth_service.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePermissionRequest(
        @NotBlank String name,
        @NotNull Long priority,
        String description
) {
}
