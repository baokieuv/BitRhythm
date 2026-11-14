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
    public RoleResponse updateRole(Long actorId, Long roleId, UpdateRoleRequest request) {
        long actorPriority = actorPriority(actorId);

        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        if (actorPriority < priorityOf(role.getPriority())) {   // sửa role cao hơn mình
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

        if (roleRepository.existsByNameIgnoreCaseAndIdNotAndIsDeleted(
                request.name(), roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        role.update(request);
        return new RoleResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long actorId, Long roleId) {
        long actorPriority = actorPriority(actorId);

        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        if (actorPriority < priorityOf(role.getPriority())) {   // xóa role cao hơn mình
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

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

    @Transactional
    public List<PermissionResponse> importPermissions(List<CreatePermissionRequest> request) {
        List<Permission> permissionsToSave = request.stream()
                .filter(req -> !permissionRepository.existsByNameIgnoreCaseAndIsDeleted(req.name(), CommonEnums.DeletedStatus.NOT_DELETED.getCode()))
                .map(Permission::new)
                .toList();

        List<Permission> savedPermissions = permissionRepository.saveAll(permissionsToSave);

        return savedPermissions.stream()
                .map(PermissionResponse::new)
                .toList();
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
    public PermissionResponse updatePermission(Long actorId, Long permissionId, UpdatePermissionRequest request) {
        long actorPriority = actorPriority(actorId);

        Permission permission = permissionRepository.findByIdAndIsDeleted(permissionId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        if (actorPriority < priorityOf(permission.getPriority())) {
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

        if (permissionRepository.existsByNameIgnoreCaseAndIdNotAndIsDeleted(
                request.name(), permissionId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        permission.update(request);
        return new PermissionResponse(permissionRepository.save(permission));
    }

    @Transactional
    public void deletePermission(Long actorId, Long permissionId) {
        long actorPriority = actorPriority(actorId);

        Permission permission = permissionRepository.findByIdAndIsDeleted(permissionId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        if (actorPriority < priorityOf(permission.getPriority())) {
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

        if (roleRepository.existsByPermissionsContainingAndIsDeleted(
                permission, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new ApiException(CoreErrorCode.RESOURCE_IN_USE);
        }

        permission.setIsDeleted(CommonEnums.DeletedStatus.DELETED.getCode());
        permissionRepository.save(permission);
    }

    // ROLE-PERMISSION ASSIGNMENTS

    @Transactional
    public void assignRoles(Long actorId, Long userId, AssignRolesRequest request) {
        long actorPriority = actorPriority(actorId);

        var user = userRepository.findByIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        var roles = roleRepository.findAllByIdInAndIsDeleted(request.roleIds(), CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        if (roles.size() != new HashSet<>(request.roleIds()).size()) {
            throw new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND); // có id sai/đã xóa
        }

        if (actorPriority < maxRolePriority(user.getRoles())   // tác động lên người cao hơn
                || actorPriority < maxRolePriority(roles)) {   // gán role cao hơn mình
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
    }

    @Transactional
    public void assignPermissions(Long actorId, Long roleId, List<Long> permissionIds) {
        long actorPriority = actorPriority(actorId);

        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));
        long rolePriority = priorityOf(role.getPriority());

        if (actorPriority < rolePriority) {               // sửa role cao hơn mình
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

        List<Permission> permissions = permissionRepository.findAllByIdInAndIsDeleted(
                permissionIds, CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        if (permissions.size() != new HashSet<>(permissionIds).size()) {
            throw new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND);
        }

        // permission > role thì không gán được (kéo theo permission <= actor)
        if (permissions.stream().anyMatch(p -> priorityOf(p.getPriority()) > rolePriority)) {
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
    }

    @Transactional
    public void removePermissions(Long actorId, Long roleId, List<Long> permissionIds) {
        long actorPriority = actorPriority(actorId);

        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));

        if (actorPriority < priorityOf(role.getPriority())) {
            throw new ApiException(CoreErrorCode.FORBIDDEN_ACCESS);
        }

        role.getPermissions().removeIf(p -> permissionIds.contains(p.getId()));
        roleRepository.save(role);
    }

    private long actorPriority(Long actorId) {
        var actor = userRepository.findByIdAndIsDeleted(actorId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));
        return maxRolePriority(actor.getRoles());
    }

    private long priorityOf(Long priority) {
        return priority == null ? 0L : priority;
    }

    private long maxRolePriority(java.util.Collection<Role> roles) {
        if (roles == null) return 0L;
        return roles.stream()
                .mapToLong(r -> priorityOf(r.getPriority()))
                .max()
                .orElse(0L);
    }
}