package com.bit_rhythm.content_service.report.dto;

import com.bit_rhythm.content_service.report.ReportReason;
import com.bit_rhythm.content_service.report.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotNull ReportTargetType targetType,
        @NotNull Long targetId,
        @NotNull ReportReason reason,
        @Size(max = 1000) String details
) {
}
