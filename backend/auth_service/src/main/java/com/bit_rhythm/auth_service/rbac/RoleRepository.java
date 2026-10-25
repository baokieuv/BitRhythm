package com.bit_rhythm.auth_service.rbac;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByNameIgnoreCaseAndIsDeleted(String name, Integer isDeleted);
    Role findByNameIgnoreCaseAndIsDeleted(String name, Integer isDeleted);
    Optional<Role> findByIdAndIsDeleted(Long id, Integer isDeleted);

    List<Role> findByIsDeleted(Integer isDeleted);
}
