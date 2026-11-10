package com.bit_rhythm.user_service.artist;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<ArtistVerificationRequest, Long> {

    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);

    Optional<ArtistVerificationRequest> findFirstByUserIdOrderByIdDesc(Long userId);

    List<ArtistVerificationRequest> findByStatusOrderByIdAsc(VerificationStatus status, Pageable pageable);
}
