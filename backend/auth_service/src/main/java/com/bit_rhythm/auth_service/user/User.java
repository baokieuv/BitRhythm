package com.bit_rhythm.auth_service.user;

import com.bit_rhythm.auth_service.rbac.Role;
import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(schema = "auth_service", name = "users")
@Getter
@Setter
public class User extends BaseEntity {
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(name = "mfa_enabled", nullable = false)
    private Boolean mfaEnabled;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            schema = "auth_service",
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
