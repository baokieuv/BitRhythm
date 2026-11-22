package com.bit_rhythm.content_service.block;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** [S2] blocker -> blocked. */
@Entity
@Table(name = "user_blocks", uniqueConstraints = @UniqueConstraint(name = "uk_block", columnNames = {"blocker_id", "blocked_id"}))
@Getter
@Setter
@NoArgsConstructor
public class UserBlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id", nullable = false)
    private Long blockerId;

    @Column(name = "blocked_id", nullable = false)
    private Long blockedId;

    public UserBlock(Long blockerId, Long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
