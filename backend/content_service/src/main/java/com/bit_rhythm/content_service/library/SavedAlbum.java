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

/** A user saved an album to their library. */
@Entity
@Table(name = "saved_albums", uniqueConstraints = @UniqueConstraint(name = "uk_saved_album", columnNames = {"user_id", "album_id"}))
@Getter
@Setter
@NoArgsConstructor
public class SavedAlbum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "album_id", nullable = false)
    private Long albumId;

    public SavedAlbum(Long userId, Long albumId) {
        this.userId = userId;
        this.albumId = albumId;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
