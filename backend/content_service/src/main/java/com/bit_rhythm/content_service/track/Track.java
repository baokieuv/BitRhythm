package com.bit_rhythm.content_service.track;

import com.bit_rhythm.content_service.common.ContentStatus;
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
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
public class Track extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "genre_id")
    private Long genreId;

    @Column(name = "cover_key")
    private String coverKey;   // falls back to the album cover when null

    /** Object key in the PRIVATE audio bucket. Set when the upload URL is issued; the client never chooses it. */
    @Column(name = "audio_key")
    private String audioKey;

    @Column(name = "audio_content_type")
    private String audioContentType;

    @Column(name = "audio_size_bytes")
    private Long audioSizeBytes;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "bitrate_kbps")
    private Integer bitrateKbps;

    @Enumerated(EnumType.STRING)
    @Column(name = "audio_status", nullable = false)
    private AudioStatus audioStatus = AudioStatus.PENDING_UPLOAD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "explicit_content", nullable = false)
    private boolean explicitContent;

    /** Denormalized counters, changed ONLY by atomic UPDATEs (TrackRepository.addPlays / addLikes). */
    @Column(name = "play_count", nullable = false)
    private long playCount;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "published_at")
    private Instant publishedAt;

    public Track(Long artistId, String title) {
        this.artistId = artistId;
        this.title = title;
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
