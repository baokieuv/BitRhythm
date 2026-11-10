package com.bit_rhythm.user_service.profile;

import com.bit_rhythm.core.entity.BaseEntity;
import com.bit_rhythm.user_service.profile.dto.UpdateProfileRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Profile of a user. The id IS the user id of auth-service (never generated here).
 * Hint: extend core's BaseEntity instead of the two timestamps below if you want its audit columns, and add
 * those columns to the Flyway script.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String bio;

    private String country;   // ISO 3166-1 alpha-2

    @Column(name = "avatar_key")
    private String avatarKey;   // object key in the avatar bucket, NOT a URL

    @Column(nullable = false)
    private boolean verified;   // [S2] verified artist

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status = ProfileStatus.ACTIVE;

    /** Kept in step with the follows table by atomic UPDATEs (see ProfileRepository), never by read-modify-write. */
    @Column(name = "follower_count", nullable = false)
    private long followerCount;

    @Column(name = "following_count", nullable = false)
    private long followingCount;

    public UserProfile(Long userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
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

    public void update(UpdateProfileRequest request) {
        this.displayName = request.displayName();
        this.bio = request.bio();
        this.country = request.country();
    }
}
