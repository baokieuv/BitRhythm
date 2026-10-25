package com.bit_rhythm.auth_service.user;

import com.bit_rhythm.auth_service.rbac.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByRolesContaining(Role role);
}
