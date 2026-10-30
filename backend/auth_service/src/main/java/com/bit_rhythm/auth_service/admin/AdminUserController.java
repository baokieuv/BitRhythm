package com.bit_rhythm.auth_service.admin;

import com.bit_rhythm.auth_service.admin.dto.UpdateUserRolesRequest;
import com.bit_rhythm.auth_service.admin.dto.UpdateUserStatusRequest;
import com.bit_rhythm.auth_service.admin.dto.UserSearchRequest;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;
    private final ResponseFactory responseFactory;

    @GetMapping
    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<ApiResponseFormat<Object>> searchUsers(
            @Valid UserSearchRequest request) {
        return responseFactory.response(AuthCode.SUCCESS, adminService.searchUsers(request));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('read:users')")
    public ResponseEntity<ApiResponseFormat<Object>> getUser(@PathVariable Long userId) {
        return responseFactory.response(AuthCode.SUCCESS, adminService.getUser(userId));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAuthority('write:users')")
    public ResponseEntity<ApiResponseFormat<Object>> updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return responseFactory.response(AuthCode.SUCCESS, adminService.updateStatus(userId, request));
    }

    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasAuthority('write:users')")
    public ResponseEntity<ApiResponseFormat<Object>> unlock(@PathVariable Long userId) {
        return responseFactory.response(AuthCode.SUCCESS, adminService.unlock(userId));
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('write:users')")
    public ResponseEntity<ApiResponseFormat<Object>> updateRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        return responseFactory.response(AuthCode.SUCCESS, adminService.updateRoles(userId, request));
    }

    @PostMapping("/{userId}/revoke-sessions")
    @PreAuthorize("hasAuthority('write:users')")
    public ResponseEntity<ApiResponseFormat<Object>> revokeSessions(@PathVariable Long userId) {
        adminService.revokeSessions(userId);
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('delete:users')")
    public ResponseEntity<ApiResponseFormat<Object>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return responseFactory.response(AuthCode.SUCCESS, null);
    }
}
