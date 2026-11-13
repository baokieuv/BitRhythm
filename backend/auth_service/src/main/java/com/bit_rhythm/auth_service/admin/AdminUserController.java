package com.bit_rhythm.auth_service.admin;

import com.bit_rhythm.auth_service.admin.dto.request.UpdateUserRolesRequest;
import com.bit_rhythm.auth_service.admin.dto.request.UpdateUserStatusRequest;
import com.bit_rhythm.auth_service.admin.dto.request.UserSearchRequest;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;
    private final ResponseFactory responseFactory;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('read:admin:users')")
    public ResponseEntity<ApiResponseFormat<Object>> searchUsers(
            @Valid @RequestBody UserSearchRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminService.searchUsers(request));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('read:admin:users')")
    public ResponseEntity<ApiResponseFormat<Object>> getUser(@PathVariable Long userId) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminService.getUser(userId));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('write:admin:users')")
    public ResponseEntity<ApiResponseFormat<Object>> updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminService.updateStatus(userId, request));
    }

    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasAuthority('write:admin:users')")
    public ResponseEntity<ApiResponseFormat<Object>> unlock(@PathVariable Long userId) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminService.unlock(userId));
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('write:admin:users')")
    public ResponseEntity<ApiResponseFormat<Object>> updateRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminService.updateRoles(userId, request));
    }

    @PostMapping("/{userId}/revoke-sessions")
    @PreAuthorize("hasAuthority('write:admin:users')")
    public ResponseEntity<ApiResponseFormat<Object>> revokeSessions(@PathVariable Long userId) {
        adminService.revokeSessions(userId);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('delete:users')")
    public ResponseEntity<ApiResponseFormat<Object>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
