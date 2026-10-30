package com.bit_rhythm.auth_service.user;

import com.bit_rhythm.auth_service.rbac.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByRolesContaining(Role role);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            WHERE u.isDeleted = :deleted
              AND (:query IS NULL OR :query = ''
                   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<User> search(@Param("deleted") Integer deleted,
                       @Param("query") String query,
                       @Param("status") String status,
                       Pageable pageable);
}
