package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.rbac.dto.request.CreatePermissionRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.PermissionSearchRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.UpdatePermissionRequest;
import com.bit_rhythm.auth_service.security.AuthUser;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/permissions")
public class PermissionController {

    private final ResponseFactory responseFactory;
    private final RbacService rbacService;

    @PostMapping
    @PreAuthorize("hasAuthority('write:admin:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.createPermission(request));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('write:admin:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> importPermissions(
            @Valid @RequestBody List<CreatePermissionRequest> request
    ) {
        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.importPermissions(request));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('read:admin:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> getAllPermissions(
            @Valid @RequestBody PermissionSearchRequest request
    ) {
        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.getAllPermissions(request));
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('read:admin:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> getPermissionById(@PathVariable Long permissionId) {
        return responseFactory.response(CoreErrorCode.SUCCESS, rbacService.getPermissionById(permissionId));
    }

    @PutMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('write:admin:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> updatePermission(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long permissionId,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        return responseFactory.response(
                CoreErrorCode.SUCCESS,
                rbacService.updatePermission(user.id(), permissionId, request)
        );
    }

    @DeleteMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('delete:admin:permissions')")
    public ResponseEntity<ApiResponseFormat<Object>> deletePermission(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long permissionId
    ) {
        rbacService.deletePermission(user.id(), permissionId);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}