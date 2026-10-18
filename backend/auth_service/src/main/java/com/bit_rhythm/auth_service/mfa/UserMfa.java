package com.bit_rhythm.auth_service.mfa;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "auth_service", name = "user_mfa")
@Getter
@Setter
public class UserMfa extends BaseEntity {
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "secret_encrypted", nullable = false)
    private String secretEncrypted;

    @Column(name = "confirmed", nullable = false)
    private Boolean confirmed;

    @Column(name = "last_used_step", nullable = true)
    private Long lastUsedStep;

    @Column(name = "confirmed_at", nullable = true)
    private LocalDateTime confirmedAt;
}
