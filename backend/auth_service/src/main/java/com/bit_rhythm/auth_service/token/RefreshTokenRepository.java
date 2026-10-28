package com.bit_rhythm.auth_service.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Find a specific token by its hash
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Retrieve all active sessions for a user
    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(Long userId);

    // Retrieve the token chain family to check for compromised token reuse
    List<RefreshToken> findAllByFamilyId(Long familyId);

    // Revoke an entire token family (used when token theft is suspected)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = CURRENT_TIMESTAMP, r.revokedReason = :reason WHERE r.familyId = :familyId AND r.revokedAt IS NULL")
    void revokeFamily(@Param("familyId") Long familyId, @Param("reason") String reason);

    // Revoke all tokens for a specific user (e.g., when they change their password)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = CURRENT_TIMESTAMP, r.revokedReason = 'USER_LOGOUT' WHERE r.userId = :userId AND r.revokedAt IS NULL")
    void revokeAllByUser(@Param("userId") Long userId);
}