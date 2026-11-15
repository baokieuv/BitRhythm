package com.bit_rhythm.content_service.report.dto;

import com.bit_rhythm.content_service.report.ContentReport;
import com.bit_rhythm.content_service.report.ReportReason;
import com.bit_rhythm.content_service.report.ReportStatus;
import com.bit_rhythm.content_service.report.ReportTargetType;

import java.time.Instant;

public record ReportResponse(
        Long id,
        Long reporterUserId,
        ReportTargetType targetType,
        Long targetId,
        ReportReason reason,
        String details,
        ReportStatus status,
        Long resolvedBy,
        String resolutionNote,
        Instant createdAt,
        Instant resolvedAt
) {
    public static ReportResponse from(ContentReport report) {
        return new ReportResponse(report.getId(), report.getReporterUserId(), report.getTargetType(), report.getTargetId(),
                report.getReason(), report.getDetails(), report.getStatus(), report.getResolvedBy(), report.getResolutionNote(),
                report.getCreatedAt(), report.getResolvedAt());
    }
}
