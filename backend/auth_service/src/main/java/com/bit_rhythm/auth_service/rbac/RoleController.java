package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.rbac.dto.request.*;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/roles")
public class RoleController {

    private final ResponseFactory responseFactory;

     private final RbacService rbacService;

    @PostMapping
    @PreAuthorize("hasAuthority('write:admin:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.createRole(request));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('read:admin:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> getAllRoles(
            @Valid @RequestBody RoleSearchRequest request) {

        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.getAllRoles(request));
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('read:admin:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> getRoleById(@PathVariable Long roleId) {

        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.getRoleById(roleId));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('write:admin:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {

        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.updateRole(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('delete:admin:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> deleteRole(@PathVariable Long roleId)
    {
        rbacService.deleteRole(roleId);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    // --- Role-Permission Management ---

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('write:admin:role_permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> assignPermissionsToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionsRequest request)
    {
        rbacService.assignPermissions(roleId, request.permissionIds());
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('write:admin:role_permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> removePermissionsFromRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RemovePermissionsRequest request)
    {
        rbacService.removePermissions(roleId, request.permissionIds());
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}