package com.bit_rhythm.auth_service.rbac.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignRolesRequest(
        @NotNull List<Long> roleIds
) {
}
