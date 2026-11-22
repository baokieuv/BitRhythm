package com.bit_rhythm.content_service.library;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** A user liked a track. Only ids: the liked tracks of a page are loaded in bulk (TrackRepository.findByIdIn). */
@Entity
@Table(name = "track_likes", uniqueConstraints = @UniqueConstraint(name = "uk_track_like", columnNames = {"user_id", "track_id"}))
@Getter
@Setter
@NoArgsConstructor
public class TrackLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    public TrackLike(Long userId, Long trackId) {
        this.userId = userId;
        this.trackId = trackId;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
