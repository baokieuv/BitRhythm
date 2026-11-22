package com.bit_rhythm.content_service.lyrics;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** [S2] Lyrics of a track (one row per track, the id IS the track id). */
@Entity
@Table(name = "track_lyrics")
@Getter
@Setter
@NoArgsConstructor
public class TrackLyrics extends BaseEntity {

    @Id
    @Column(name = "track_id")
    private Long trackId;

    @Column(name = "plain_text", columnDefinition = "longtext")
    private String plainText;

    /** LRC format, one line per lyric line: "[01:23.45] text". Lets the player highlight the current line. */
    @Column(name = "synced_lrc", columnDefinition = "longtext")
    private String syncedLrc;

    public TrackLyrics(Long trackId) {
        this.trackId = trackId;
    }

    public void update(String plainText, String syncedLrc) {
        this.plainText = plainText;
        this.syncedLrc = syncedLrc;
    }


    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
