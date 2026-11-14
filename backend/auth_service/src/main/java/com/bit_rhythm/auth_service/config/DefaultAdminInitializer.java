package com.bit_rhythm.auth_service.config;

import com.bit_rhythm.auth_service.common.enums.UserEventType;
import com.bit_rhythm.auth_service.common.enums.UserStatus;
import com.bit_rhythm.auth_service.event.UserEventPublisher;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultAdminInitializer {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ARTIST_ROLE = "ARTIST";
    private static final String USER_ROLE = "USER";

    // Higher number = more authority. SUPER_ADMIN > ADMIN, so ADMIN cannot touch SUPER_ADMIN.
    private static final long SUPER_ADMIN_PRIORITY = 1000L;
    private static final long ADMIN_PRIORITY = 500L;
    private static final long ARTIST_PRIORITY = 10L;
    private static final long USER_PRIORITY = 1L;

    // Shared by SUPER_ADMIN and ADMIN. Permission priority stays at the default 0,
    // so it is never higher than any role's priority.
    private static final List<String> ADMIN_PERMISSIONS = List.of(
            // auth-service: roles / permissions / assignments
            "read:admin:roles",
            "write:admin:roles",
            "delete:admin:roles",
            "read:admin:permissions",
            "write:admin:permissions",
            "delete:admin:permissions",
            "write:admin:role_assignments",
            "write:admin:role_permissions",
            // auth-service + user-service: users
            "read:admin:users",
            "write:admin:users",
            "delete:users",                       // as written in AdminUserController (auth)
            // auth-service: service clients
            "read:admin:service-clients",
            "write:admin:service-clients",
            "write:service-clients",              // as written in AdminServiceClientController.deleteClient
            // notification-service
            "read:admin:notification-logs",
            "write:admin:notification-logs",
            "read:admin:templates",
            "write:admin:templates",
            "delete:admin:templates",
            // user-service: artist verification
            "read:admin:artist-verifications",
            "write:admin:artist-verifications"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    private final AppProperties appProperties;

    @PostConstruct
    public void createDefaultAdmin() {
        AppProperties.Admin adminProperties = appProperties.admin();

        Set<Permission> adminPermissions = new LinkedHashSet<>();
        for (String permissionName : ADMIN_PERMISSIONS) {
            adminPermissions.add(findOrCreatePermission(permissionName, ADMIN_PRIORITY));
        }

        Role superAdminRole = findOrCreateRole(
                SUPER_ADMIN_ROLE, "Super administrator role", SUPER_ADMIN_PRIORITY, adminPermissions);
        findOrCreateRole(ADMIN_ROLE, "System administrator role", ADMIN_PRIORITY, adminPermissions);
        findOrCreateRole(ARTIST_ROLE, "Artist role", ARTIST_PRIORITY, Set.of());   // no permissions
        findOrCreateRole(USER_ROLE, "Default user role", USER_PRIORITY, Set.of()); // no permissions

        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(adminProperties.email());

        if (existingUser.isPresent()) {
            User admin = existingUser.get();
            admin.setIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
            admin.getRoles().add(superAdminRole);
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
                .roles(new LinkedHashSet<>(Set.of(superAdminRole)))
                .build();

        transactionTemplate.executeWithoutResult(status -> {
            userRepository.save(admin);
            eventPublisher.publishUserEvent(admin, UserEventType.USER_REGISTERED);
        });
        log.info("Created default admin account: {}", adminProperties.email());
    }

    private Permission findOrCreatePermission(String name, long priority) {
        return permissionRepository.findByNameIgnoreCaseAndIsDeleted(
                        name, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseGet(() -> {
                    Permission permission = permissionRepository.findAll().stream()
                            .filter(item -> name.equalsIgnoreCase(item.getName()))
                            .findFirst()
                            .orElseGet(() -> new Permission(
                                    new CreatePermissionRequest(name, priority, "Permission to " + name.replace(':', ' '))));
                    permission.setName(name); // keep lowercase: hasAuthority() is case-sensitive
                    permission.setDescription("Permission to " + name.replace(':', ' '));
                    permission.setIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
                    return permissionRepository.save(permission);
                });
    }

    private Role findOrCreateRole(String name, String description, long priority, Set<Permission> permissions) {
        Role role = roleRepository.findByNameIgnoreCaseAndIsDeleted(
                name, CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        if (role == null) {
            role = roleRepository.findAll().stream()
                    .filter(item -> name.equalsIgnoreCase(item.getName()))
                    .findFirst()
                    .orElseGet(() -> new Role(new CreateRoleRequest(name, priority, description)));
            role.setName(name);
            role.setDescription(description);
            role.setIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        }
        role.setPriority(priority);
        role.getPermissions().addAll(permissions);
        return roleRepository.save(role);
    }
}