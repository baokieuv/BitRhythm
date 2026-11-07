package com.bit_rhythm.auth_service.admin;

import com.bit_rhythm.auth_service.admin.dto.response.AdminUserResponse;
import com.bit_rhythm.auth_service.admin.dto.request.UpdateUserRolesRequest;
import com.bit_rhythm.auth_service.admin.dto.request.UpdateUserStatusRequest;
import com.bit_rhythm.auth_service.admin.dto.request.UserSearchRequest;
import com.bit_rhythm.auth_service.common.enums.RevokeReason;
import com.bit_rhythm.auth_service.rbac.RoleRepository;
import com.bit_rhythm.auth_service.token.RefreshTokenService;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.user.UserRepository;
import com.bit_rhythm.auth_service.common.enums.UserStatus;
import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.core.exception.ApiException;
import com.bit_rhythm.core.exception.CoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> searchUsers(UserSearchRequest request) {
        String status = request.getStatus() == null ? null : request.getStatus().toUpperCase();
        return userRepository.search(
                CommonEnums.DeletedStatus.NOT_DELETED.getCode(),
                blankToNull(request.getQuery()),
                status,
                request.toPageable()).map(AdminUserResponse::new);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long userId) {
        return new AdminUserResponse(findUser(userId));
    }

    @Transactional
    public AdminUserResponse updateStatus(Long userId, UpdateUserStatusRequest request) {
        User user = findUser(userId);
        UserStatus status = request.status();
        user.setStatus(status.name());
        if (status != UserStatus.LOCKED) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        } else if (user.getLockedUntil() == null || !user.getLockedUntil().isAfter(LocalDateTime.now())) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
        }
        User saved = userRepository.save(user);
        if (status == UserStatus.DISABLED || status == UserStatus.LOCKED) {
            refreshTokenService.revokeAll(userId, RevokeReason.ROTATED);
        }
        return new AdminUserResponse(saved);
    }

    @Transactional
    public AdminUserResponse unlock(Long userId) {
        User user = findUser(userId);
        user.setStatus(UserStatus.ACTIVE.name());
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        return new AdminUserResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse updateRoles(Long userId, UpdateUserRolesRequest request) {
        User user = findUser(userId);
        var roles = roleRepository.findAllByIdInAndIsDeleted(
                request.roleIds(), CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        if (roles.size() != new HashSet<>(request.roleIds()).size()) {
            throw new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND);
        }
        user.setRoles(new HashSet<>(roles));
        User saved = userRepository.save(user);
        refreshTokenService.revokeAll(userId, RevokeReason.ROTATED);
        return new AdminUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findUser(userId);
        user.setIsDeleted(CommonEnums.DeletedStatus.DELETED.getCode());
        user.setStatus(UserStatus.DISABLED.name());
        userRepository.save(user);
        refreshTokenService.revokeAll(userId, RevokeReason.ROTATED);
    }

    @Transactional
    public void revokeSessions(Long userId) {
        findUser(userId);
        refreshTokenService.revokeAll(userId, RevokeReason.ROTATED);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getIsDeleted().equals(CommonEnums.DeletedStatus.NOT_DELETED.getCode()))
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
