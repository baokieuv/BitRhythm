package com.bit_rhythm.user_service.follow;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followee_id"}))
@Getter @NoArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;                    // chỉ id, không @ManyToOne

    @Column(name = "followee_id", nullable = false)
    private Long followeeId;

    private LocalDateTime createdAt;

    public Follow(Long followerId, Long followeeId) {
        this.followerId = followerId;
        this.followeeId = followeeId;
    }

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
