package com.bit_rhythm.user_service.settings;

import com.bit_rhythm.core.entity.BaseEntity;
import com.bit_rhythm.user_service.settings.dto.SettingsRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** One row per user, same id as UserProfile (no JPA relation on purpose: just the id). */
@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
public class UserSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    /** "vi" | "en". notification-service uses it to choose the language of notifications. */
    @Column(nullable = false)
    private String locale = "en";

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false)
    private ProfileVisibility visibility = ProfileVisibility.PUBLIC;

    @Column(name = "show_listening_activity", nullable = false)
    private boolean showListeningActivity = true;

    public UserSettings(Long userId) {
        this.userId = userId;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public void update(SettingsRequest request) {
        this.locale = request.locale();
        this.visibility = request.visibility();
        this.showListeningActivity = request.showListeningActivity();
    }
}
