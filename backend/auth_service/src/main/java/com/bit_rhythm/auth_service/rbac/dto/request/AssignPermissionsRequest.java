package com.bit_rhythm.auth_service.rbac.dto.request;

import java.util.List;

public record AssignPermissionsRequest(List<Long> permissionIds) {
}
