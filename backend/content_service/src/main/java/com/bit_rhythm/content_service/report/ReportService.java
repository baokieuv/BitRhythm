package com.bit_rhythm.content_service.report;

import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.report.dto.CreateReportRequest;
import com.bit_rhythm.content_service.report.dto.ReportResponse;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    /**
     * [S2] TODO(hint): the target must exist (TRACK_NOT_FOUND / ALBUM_NOT_FOUND / PLAYLIST_NOT_FOUND) and be visible to the reporter;
     * a user cannot report their own content; an OPEN report of the same user on the same target -> REPORT_ALREADY_OPEN (check, and map the
     * duplicate-key race to the same error). Store the report only: nothing is hidden automatically (a malicious mass-report must not remove content).
     */
    public ReportResponse create(Long reporterUserId, CreateReportRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): admin queue, oldest first. cursor = id of the last report received (null -> 0), size + 1 rows. */
    public CursorPage<ReportResponse> queue(ReportStatus status, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint), ONE transaction: the report must be OPEN (REPORT_ALREADY_CLOSED); status RESOLVED + resolvedBy/resolvedAt/note. With
     * takeDown, call AdminCatalogService.takeDownTrack / takeDownAlbum. Also close the OTHER open reports of the same target
     * (findByTargetTypeAndTargetIdAndStatus) as RESOLVED, they are answered by the same decision.
     */
    public ReportResponse resolve(Long reportId, Long adminId, String note, boolean takeDown) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): OPEN -> DISMISSED with the note; REPORT_ALREADY_CLOSED otherwise. */
    public ReportResponse dismiss(Long reportId, Long adminId, String note) {
        throw new UnsupportedOperationException("TODO");
    }
}
