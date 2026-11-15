package com.bit_rhythm.auth_service.rbac;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByNameIgnoreCaseAndIdNotAndIsDeleted(String name, Long id, Integer isDeleted);

    boolean existsByNameIgnoreCaseAndIsDeleted(String name, Integer isDeleted);

    Optional<Role> findByNameIgnoreCaseAndIsDeleted(String name, Integer isDeleted);

    List<Role> findAllByNameInIgnoreCaseAndIsDeleted(List<String> names, Integer isDeleted);

    Optional<Role> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByPermissionsContainingAndIsDeleted(Permission permission, Integer isDeleted);

    List<Role> findAllByIdInAndIsDeleted(List<Long> ids, Integer isDeleted);

    @Query("SELECT r FROM Role r WHERE r.isDeleted = :status " +
            "AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:description IS NULL OR LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Role> searchRoles(@Param("status") Integer status,
                           @Param("name") String name,
                           @Param("description") String description,
                           Pageable pageable);
}
