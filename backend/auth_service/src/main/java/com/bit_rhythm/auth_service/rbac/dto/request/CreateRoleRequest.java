package com.bit_rhythm.auth_service.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        String name,
        @NotNull(message = "Role priority is required")
        Long priority,
        String description
) {}
