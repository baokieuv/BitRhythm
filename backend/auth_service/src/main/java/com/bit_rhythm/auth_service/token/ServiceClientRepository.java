package com.bit_rhythm.auth_service.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceClientRepository extends JpaRepository<ServiceClient, Long> {

    Optional<ServiceClient> findByClientIdAndIsActiveTrue(String clientId);

    boolean existsByClientId(String clientId);
}