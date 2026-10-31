package com.bit_rhythm.auth_service.config;

import com.bit_rhythm.auth_service.common.enums.UserStatus;
import com.bit_rhythm.auth_service.rbac.Permission;
import com.bit_rhythm.auth_service.rbac.PermissionRepository;
import com.bit_rhythm.auth_service.rbac.Role;
import com.bit_rhythm.auth_service.rbac.RoleRepository;
import com.bit_rhythm.auth_service.rbac.dto.request.CreatePermissionRequest;
import com.bit_rhythm.auth_service.rbac.dto.request.CreateRoleRequest;
import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.user.UserRepository;
import com.bit_rhythm.core.common.CommonEnums;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultAdminInitializer {

    private static final String ADMIN_ROLE = "ADMIN";

    private static final List<String> ADMIN_PERMISSIONS = List.of(
            "read:roles",
            "write:roles",
            "delete:roles",
            "read:permissions",
            "write:permissions",
            "delete:permissions",
            "write:role_permissions",
            "read:users",
            "write:users",
            "delete:users"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @PostConstruct
    public void createDefaultAdmin() {
        AppProperties.Admin adminProperties = appProperties.admin();
        Set<Permission> permissions = new LinkedHashSet<>();
        for (String permissionName : ADMIN_PERMISSIONS) {
            permissions.add(findOrCreatePermission(permissionName));
        }

        Role adminRole = findOrCreateAdminRole(permissions);
        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(adminProperties.email());

        if (existingUser.isPresent()) {
            User admin = existingUser.get();
            admin.setIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
            log.info("Default admin account is ready: {}", adminProperties.email());
            return;
        }

        User admin = User.builder()
                .username(adminProperties.username())
                .email(adminProperties.email())
                .passwordHash(passwordEncoder.encode(adminProperties.password()))
                .status(UserStatus.ACTIVE.name())
                .emailVerified(true)
                .mfaEnabled(false)
                .failedLoginAttempts(0)
                .version(0L)
                .passwordChangedAt(LocalDateTime.now())
                .roles(new LinkedHashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);
        log.info("Created default admin account: {}", adminProperties.email());
    }

    private Permission findOrCreatePermission(String name) {
        return permissionRepository.findByNameIgnoreCaseAndIsDeleted(
                        name, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseGet(() -> {
                    Permission permission = permissionRepository.findAll().stream()
                            .filter(item -> name.equalsIgnoreCase(item.getName()))
                            .findFirst()
                            .orElseGet(() -> new Permission(
                                    new CreatePermissionRequest(name, "Permission to " + name.replace(':', ' '))));
                    permission.setName(name);
                    permission.setDescription("Permission to " + name.replace(':', ' '));
                    permission.setIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
                    return permissionRepository.save(permission);
                });
    }

    private Role findOrCreateAdminRole(Set<Permission> permissions) {
        Role adminRole = roleRepository.findByNameIgnoreCaseAndIsDeleted(
                ADMIN_ROLE, CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        if (adminRole == null) {
            adminRole = roleRepository.findAll().stream()
                    .filter(item -> ADMIN_ROLE.equalsIgnoreCase(item.getName()))
                    .findFirst()
                    .orElseGet(() -> new Role(
                            new CreateRoleRequest(ADMIN_ROLE, "System administrator role")));
            adminRole.setName(ADMIN_ROLE);
            adminRole.setDescription("System administrator role");
            adminRole.setIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        }
        adminRole.getPermissions().addAll(permissions);
        return roleRepository.save(adminRole);
    }
}
