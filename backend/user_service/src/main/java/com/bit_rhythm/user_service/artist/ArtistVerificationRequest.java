package com.bit_rhythm.user_service.artist;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** [S2] */
@Entity
@Table(name = "artist_verification_requests")
@Getter @Setter @NoArgsConstructor
public class ArtistVerificationRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING) private VerificationStatus status = VerificationStatus.PENDING;
    private String message;
    private Long reviewedBy;
    private String reviewNote;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime reviewedAt;
}
