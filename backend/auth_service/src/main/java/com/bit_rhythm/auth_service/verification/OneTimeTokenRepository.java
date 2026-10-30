package com.bit_rhythm.auth_service.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OneTimeTokenRepository extends JpaRepository<OneTimeToken, Long> {
    Optional<OneTimeToken> findByTokenHashAndType(String tokenHash, String type);
}
