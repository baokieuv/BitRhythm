package com.bit_rhythm.auth_service.rbac;

import com.bit_rhythm.auth_service.rbac.dto.request.CreateRoleRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.UpdateRoleRequest;
import com.bit_rhythm.auth_service.rbac.dto.response.RoleResponse;
import com.bit_rhythm.auth_service.user.UserRepository;
import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.core.exception.ApiException;
import com.bit_rhythm.core.exception.CoreErrorCode;
import lombok.RequiredArgsConstructor;
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
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findByIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        return roles.stream().map(RoleResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long roleId) {
        Role role = roleRepository.findByIdAndIsDeleted(roleId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));


        return new RoleResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        if (roleRepository.existsByNameIgnoreCaseAndIsDeleted(request.name(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
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

    // --- Role-Permission Management ---

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