package com.bit_rhythm.auth_service.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateUserRolesRequest(@NotEmpty List<Long> roleIds) {
}
