package com.bit_rhythm.content_service.playlist;

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

/** A track in a playlist. Only ids, no @ManyToOne: tracks of a page are loaded in bulk (TrackRepository.findByIdIn). */
@Entity
@Table(name = "playlist_tracks", uniqueConstraints = @UniqueConstraint(name = "uk_playlist_track", columnNames = {"playlist_id", "track_id"}))
@Getter
@Setter
@NoArgsConstructor
public class PlaylistTrack extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    /** 1, 2, 3 ... in play order. Appending uses max + 1; reordering rewrites the positions in one transaction. */
    @Column(nullable = false)
    private int position;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public PlaylistTrack(Long playlistId, Long trackId, int position) {
        this.playlistId = playlistId;
        this.trackId = trackId;
        this.position = position;
    }

    @PrePersist
    void onCreate() {
        addedAt = Instant.now();
    }
}
