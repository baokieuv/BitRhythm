package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.rbac.dto.request.*;
import com.bit_rhythm.auth_service.rbac.dto.response.PermissionResponse;
import com.bit_rhythm.auth_service.rbac.dto.response.RoleResponse;
import com.bit_rhythm.auth_service.user.UserRepository;
import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.core.exception.ApiException;
import com.bit_rhythm.core.exception.CoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    // ROLE MANAGEMENT

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByNameIgnoreCaseAndIsDeleted(request.name(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        Role role = new Role(request);
        Role savedRole = roleRepository.save(role);
        return new RoleResponse(savedRole);
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(RoleSearchRequest request) {
        Page<Role> rolePage = roleRepository.searchRoles(
                CommonEnums.DeletedStatus.NOT_DELETED.getCode(),
                request.getName(),
                request.getDescription(),
                request.toPageable()
        );

        return rolePage.map(RoleResponse::new);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        return new RoleResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        if (roleRepository.existsByNameIgnoreCaseAndIdNotInAndIsDeleted(request.name(), roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        role.update(request);

        Role savedRole = roleRepository.save(role);
        return new RoleResponse(savedRole);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        if (userRepository.existsByRolesContaining(role)) {
            throw new ApiException(CoreErrorCode.RESOURCE_IN_USE);
        }

        role.setIsDeleted(CommonEnums.DeletedStatus.DELETED.getCode());
        roleRepository.save(role);
    }

    // PERMISSION MANAGEMENT

    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        if (permissionRepository.existsByNameIgnoreCaseAndIsDeleted(request.name(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        Permission permission = new Permission(request);
        Permission savedPermission = permissionRepository.save(permission);
        return new PermissionResponse(savedPermission);
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> getAllPermissions(PermissionSearchRequest request) {
        Page<Permission> permissionPage = permissionRepository.searchPermissions(
                CommonEnums.DeletedStatus.NOT_DELETED.getCode(),
                request.getName(),
                request.getDescription(),
                request.toPageable()
        );
        return permissionPage.map(PermissionResponse::new);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long permissionId) {
        Permission permission = permissionRepository.findByIdAndIsDeleted(permissionId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        return new PermissionResponse(permission);
    }

    @Transactional
    public PermissionResponse updatePermission(Long permissionId, UpdatePermissionRequest request) {
        if (permissionRepository.existsByNameIgnoreCaseAndIdNotInAndIsDeleted(request.name(), permissionId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        Permission permission = permissionRepository.findByIdAndIsDeleted(permissionId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        permission.update(request);

        Permission savedPermission = permissionRepository.save(permission);
        return new PermissionResponse(savedPermission);
    }

    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findByIdAndIsDeleted(permissionId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        // Prevent deletion if the permission is currently assigned to any active roles
        if (roleRepository.existsByPermissionsContainingAndIsDeleted(permission, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_IN_USE);
        }

        permission.setIsDeleted(CommonEnums.DeletedStatus.DELETED.getCode());
        permissionRepository.save(permission);
    }

    // ROLE-PERMISSION ASSIGNMENTS

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        List<Permission> permissions = permissionRepository.findAllByIdInAndIsDeleted(permissionIds, CommonEnums.DeletedStatus.NOT_DELETED.getCode());

        role.getPermissions().addAll(new HashSet<>(permissions));

        roleRepository.save(role);
    }

    @Transactional
    public void removePermissions(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        role.getPermissions().removeIf(permission -> permissionIds.contains(permission.getId()));

        roleRepository.save(role);
    }
}