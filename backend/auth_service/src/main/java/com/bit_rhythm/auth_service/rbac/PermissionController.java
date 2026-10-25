package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.rbac.dto.request.CreatePermissionRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.PermissionSearchRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.UpdatePermissionRequest;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
public class PermissionController {

    private final ResponseFactory responseFactory;
    private final RbacService rbacService;

    @PostMapping
    @PreAuthorize("hasAuthority('write:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        return responseFactory.response(AuthCode.SUCCESS, rbacService.createPermission(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('read:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> getAllPermissions(
            @Valid @RequestBody PermissionSearchRequest request
    ) {
        return responseFactory.response(AuthCode.SUCCESS, rbacService.getAllPermissions(request));
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('read:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> getPermissionById(@PathVariable Long permissionId) {
        return responseFactory.response(AuthCode.SUCCESS, rbacService.getPermissionById(permissionId));
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('write:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> updatePermission(
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return responseFactory.response(AuthCode.SUCCESS, rbacService.updatePermission(permissionId, request));
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('delete:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> deletePermission(@PathVariable Long permissionId) {
        rbacService.deletePermission(permissionId);
        return responseFactory.response(AuthCode.SUCCESS, null);
    }
}