package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** follower -> followee. Only ids, no @ManyToOne: names are looked up in bulk (ProfileRepository.findByUserIdIn). */
@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(name = "uk_follow", columnNames = {"follower_id", "followee_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "followee_id", nullable = false)
    private Long followeeId;

    public Follow(Long followerId, Long followeeId) {
        this.followerId = followerId;
        this.followeeId = followeeId;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
