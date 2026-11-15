package com.bit_rhythm.auth_service.rbac.dto.request;

import java.util.List;

public record AssignRolesRequest(
        List<Long> roleIds,
        List<String> roleNames
) {
}
