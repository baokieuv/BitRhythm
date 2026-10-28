package com.bit_rhythm.auth_service.token;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "auth_service", name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at", nullable = true)
    private LocalDateTime revokedAt;

    @Column(name = "revoked_reason", nullable = true)
    private String revokedReason;

    @Column(name = "revoked_by", nullable = true)
    private Long revokedBy;

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    @Column(name = "user_agent", nullable = true)
    private String userAgent;
}
