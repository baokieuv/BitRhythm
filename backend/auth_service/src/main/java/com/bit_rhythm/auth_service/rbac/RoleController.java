package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.rbac.dto.request.AssignPermissionsRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.CreateRoleRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.RemovePermissionsRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.UpdateRoleRequest;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {

    private final ResponseFactory responseFactory;

     private final RbacService rbacService;

    @PostMapping
    @PreAuthorize("hasAuthority('write:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        return responseFactory.response(AuthCode.SUCCESS, rbacService.createRole(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('read:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> getAllRoles() {

        return responseFactory.response(AuthCode.SUCCESS, rbacService.getAllRoles());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('read:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> getRoleById(@PathVariable Long roleId) {

        return responseFactory.response(AuthCode.SUCCESS, rbacService.getRoleById(roleId));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('write:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {

        return responseFactory.response(AuthCode.SUCCESS, rbacService.updateRole(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('delete:roles')")
    public ResponseEntity<ApiResponseFormat<Object>> deleteRole(@PathVariable Long roleId)
    {
        rbacService.deleteRole(roleId);
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    // --- Role-Permission Management ---

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('write:role_permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> assignPermissionsToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionsRequest request)
    {
        rbacService.assignPermissions(roleId, request.permissionIds());
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('write:role_permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> removePermissionsFromRole(
            @PathVariable Long roleId,
            @Valid @RequestBody RemovePermissionsRequest request)
    {
        rbacService.removePermissions(roleId, request.permissionIds());
        return responseFactory.response(AuthCode.SUCCESS, null);
    }
}