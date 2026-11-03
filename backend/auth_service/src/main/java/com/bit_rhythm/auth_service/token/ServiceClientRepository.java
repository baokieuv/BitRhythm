package com.bit_rhythm.auth_service.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceClientRepository extends JpaRepository<ServiceClient, Long> {

    Optional<ServiceClient> findByClientIdAndIsActiveTrueAndIsDeleted(String clientId, Integer isDeleted);

    Optional<ServiceClient> findByClientIdAndIsDeleted(String clientId, Integer isDeleted);

    boolean existsByClientId(String clientId);
}
