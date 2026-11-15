package com.bit_rhythm.content_service.report;

import com.bit_rhythm.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** [S2] A user reports a track, album or playlist (copyright, abuse...). */
@Entity
@Table(name = "content_reports")
@Getter
@Setter
@NoArgsConstructor
public class ContentReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "resolution_note")
    private String resolutionNote;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public ContentReport(Long reporterUserId, ReportTargetType targetType, Long targetId, ReportReason reason, String details) {
        this.reporterUserId = reporterUserId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.details = details;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
