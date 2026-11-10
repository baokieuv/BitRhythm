package com.bit_rhythm.user_service.artist;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** [S2] A user asks to become a verified artist (role ARTIST in auth-service). One PENDING request per user at a time. */
@Entity
@Table(name = "artist_verification_requests")
@Getter
@Setter
@NoArgsConstructor
public class ArtistVerificationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status = VerificationStatus.PENDING;

    private String message;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public ArtistVerificationRequest(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
