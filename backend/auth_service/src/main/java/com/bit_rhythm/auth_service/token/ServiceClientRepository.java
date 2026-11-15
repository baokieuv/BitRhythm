package com.bit_rhythm.auth_service.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceClientRepository extends JpaRepository<ServiceClient, Long> {

    Optional<ServiceClient> findByClientIdAndIsActiveTrueAndIsDeleted(String clientId, Integer isDeleted);

    Optional<ServiceClient> findByClientIdAndIsDeleted(String clientId, Integer isDeleted);

    List<ServiceClient> findAllByIsDeleted(Integer isDeleted);

    boolean existsByClientIdAndIsDeleted(String clientId, Integer isDeleted);
}
