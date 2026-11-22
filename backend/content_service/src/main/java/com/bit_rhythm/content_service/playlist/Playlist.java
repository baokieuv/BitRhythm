package com.bit_rhythm.content_service.playlist;

import com.bit_rhythm.content_service.playlist.dto.PlaylistRequest;
import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "playlists")
@Getter
@Setter
@NoArgsConstructor
public class Playlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;   // user id of auth-service

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaylistVisibility visibility = PlaylistVisibility.PRIVATE;

    /** Kept in step with playlist_tracks by atomic UPDATEs (PlaylistRepository.addTracks), never by read-modify-write. */
    @Column(name = "track_count", nullable = false)
    private int trackCount;

    public Playlist(PlaylistRequest request, Long ownerUserId) {
        this.ownerUserId = ownerUserId;
        this.name = request.name();
        this.description = request.description();
        this.visibility = request.visibility();
        this.trackCount = 0;
    }

    public void update(PlaylistRequest request) {
        this.name = request.name();
        this.description = request.description();
        this.visibility = request.visibility();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
