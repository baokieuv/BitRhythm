package com.bit_rhythm.user_service.block;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** [S2] */
@Entity
@Table(name = "user_blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"}))
@Getter @NoArgsConstructor
public class UserBlock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "blocker_id", nullable = false) private Long blockerId;
    @Column(name = "blocked_id", nullable = false) private Long blockedId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserBlock(Long blockerId, Long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }
}
