package com.bit_rhythm.content_service.history;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** One counted play of a track by a user: feeds "recently played" and the recommendations. The table grows fast: see HistoryRetentionJob. */
@Entity
@Table(name = "play_history")
@Getter
@Setter
@NoArgsConstructor
public class PlayHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "played_at", nullable = false, updatable = false)
    private Instant playedAt;

    public PlayHistory(Long userId, Long trackId) {
        this.userId = userId;
        this.trackId = trackId;
    }

    @PrePersist
    void onCreate() {
        playedAt = Instant.now();
    }
}
