package com.bit_rhythm.auth_service.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OneTimeTokenRepository extends JpaRepository<OneTimeToken, Long> {
    Optional<OneTimeToken> findByTokenHashAndType(String tokenHash, String type);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OneTimeToken t set t.usedAt = :now " +
            "where t.userId = :userId and t.type = :type and t.usedAt is null")
    void invalidateActiveTokens(@Param("userId") Long userId,
                               @Param("type") String type,
                               @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("update OneTimeToken t set t.usedAt = :now " +
            "where t.id = :id and t.usedAt is null and t.expiresAt > :now")
    int markAsUsed(@Param("id") Long id, @Param("now") LocalDateTime now);
}
