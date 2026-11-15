package com.bit_rhythm.content_service.artist;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.time.LocalDateTime;

/**
 * Artist page of a user with the role ARTIST. ownerUserId is the user id of auth-service (one artist per user). The name is
 * copied here on purpose: listings of tracks show it without calling user-service.
 * Hint: extend core's BaseEntity instead of the two timestamps if you want its audit columns, and add them to the Flyway script.
 */
@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
public class Artist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(nullable = false)
    private String name;

    private String bio;

    @Column(name = "image_key")
    private String imageKey;   // object key in the cover bucket, NOT a URL

    public Artist(Long ownerUserId, String name) {
        this.ownerUserId = ownerUserId;
        this.name = name;
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
