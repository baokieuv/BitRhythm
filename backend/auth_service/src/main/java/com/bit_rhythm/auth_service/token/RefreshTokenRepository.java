package com.bit_rhythm.auth_service.token;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshToken r WHERE r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    // Retrieve all active sessions for a user
    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(Long userId);

    // Retrieve the token chain family to check for compromised token reuse
    List<RefreshToken> findAllByFamilyId(Long familyId);

    /**
     * Revoke every still-active token of a family (used when token theft is suspected).
     * flush + clear keep the persistence context consistent with the bulk update.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now, r.revokedReason = :reason " +
            "WHERE r.familyId = :familyId AND r.revokedAt IS NULL")
    int revokeFamily(@Param("familyId") Long familyId,
                     @Param("reason") String reason,
                     @Param("now") LocalDateTime now);

    /** Revoke every still-active token of a user (password change, logout-all, ...). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now, r.revokedReason = :reason " +
            "WHERE r.userId = :userId AND r.revokedAt IS NULL")
    int revokeAllByUser(@Param("userId") Long userId,
                        @Param("reason") String reason,
                        @Param("now") LocalDateTime now);

    /** Housekeeping: delete tokens that expired before the given threshold. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :threshold")
    int deleteExpiredBefore(@Param("threshold") LocalDateTime threshold);
}