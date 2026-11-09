package com.bit_rhythm.user_service.profile;

import com.bit_rhythm.user_service.profile.dto.UpdateProfileRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter @Setter @NoArgsConstructor
public class UserProfile {

    @Id
    private Long userId;                        // NOT @GeneratedValue

    private String displayName;
    private String bio;
    private String country;
    private String avatarKey;
    private boolean verified;

    @Enumerated(EnumType.STRING)
    private ProfileStatus status = ProfileStatus.ACTIVE;

    private long followerCount;
    private long followingCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Tạo từ JWT: displayName mặc định = phần trước dấu @. */
    public static UserProfile create(Long userId, String email) {
        var p = new UserProfile();
        p.userId = userId;
        p.displayName = (email != null && email.contains("@"))
                ? email.substring(0, email.indexOf('@')) : "user" + userId;
        return p;
    }

    /** Chỉ đổi displayName, bio, country. */
    public void update(UpdateProfileRequest r) {
        this.displayName = r.displayName();
        this.bio = r.bio();
        this.country = r.country();
    }

    @PrePersist
    void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }
}
