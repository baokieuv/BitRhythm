package com.bit_rhythm.content_service.report;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ContentReport, Long> {

    /** One OPEN report per reporter and target: a second one is REPORT_ALREADY_OPEN. */
    boolean existsByReporterUserIdAndTargetTypeAndTargetIdAndStatus(
            Long reporterUserId, ReportTargetType targetType, Long targetId, ReportStatus status);

    // The moderators' queue works oldest first: cursor = id of the last row received, 0 for the first page. Ask for size + 1 rows.
    List<ContentReport> findByStatusAndIdGreaterThanOrderByIdAsc(ReportStatus status, Long cursor, Pageable pageable);

    List<ContentReport> findByTargetTypeAndTargetIdAndStatus(ReportTargetType targetType, Long targetId, ReportStatus status);
}
