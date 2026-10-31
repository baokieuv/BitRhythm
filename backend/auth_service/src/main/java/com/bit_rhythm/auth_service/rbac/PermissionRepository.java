package com.bit_rhythm.auth_service.rbac;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long>
{
    Optional<Permission> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByNameIgnoreCaseAndIsDeleted(String name, Integer isDeleted);

    boolean existsByNameIgnoreCaseAndIdNotAndIsDeleted(String name, Long id, Integer isDeleted);

    List<Permission> findAllByIdInAndIsDeleted(List<Long> ids, Integer isDeleted);

    @Query("SELECT p FROM Permission p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:description IS NULL OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
            "(:isDeleted IS NULL OR p.isDeleted = :isDeleted)")
    Page<Permission> searchPermissions(@Param("isDeleted") Integer isDeleted,
                                       @Param("name") String name,
                                       @Param("description") String description,
                                       Pageable pageable);
}
