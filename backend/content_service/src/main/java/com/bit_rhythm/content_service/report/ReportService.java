package com.bit_rhythm.content_service.report;

import com.bit_rhythm.content_service.admin.AdminCatalogService;
import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.artist.ArtistRepository;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.playlist.Playlist;
import com.bit_rhythm.content_service.playlist.PlaylistRepository;
import com.bit_rhythm.content_service.playlist.PlaylistVisibility;
import com.bit_rhythm.content_service.report.dto.CreateReportRequest;
import com.bit_rhythm.content_service.report.dto.ReportResponse;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reports of abusive content. A report is only RECORDED: nothing is hidden automatically, so a malicious mass-report cannot
 * remove content. Only an admin decision (resolve with takeDown) changes the content.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final int NOT_DELETED = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

    /** Admin queue page size when the caller does not bound it further; clamped below. */
    private static final int MAX_QUEUE_PAGE = 50;

    private final ReportRepository reportRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final ArtistRepository artistRepository;
    private final AdminCatalogService adminCatalogService;

    /**
     * Report a track, album or playlist. NOT @Transactional on purpose (design doc, section 12): two identical requests can race
     * on the unique key of OPEN reports, so the save is its own transaction and the violation is caught here and mapped to the
     * same error as the pre-check.
     *
     * The reporter is NOT subject to block rules: someone blocked by an artist must still be able to report that artist's content.
     */
    public ReportResponse create(Long reporterUserId, CreateReportRequest request) {

        Target target = loadTarget(request.targetType(), request.targetId());

        if (reporterUserId.equals(target.ownerUserId())) {
            throw new ContentException(ContentErrorCode.CANNOT_REPORT_OWN_CONTENT);
        }

        // not visible to the reporter (draft, hidden, taken down, private): same answer as "does not exist"
        if (!target.visible()) {
            throw notFound(request.targetType());
        }

        if (reportRepository.existsByReporterUserIdAndTargetTypeAndTargetIdAndStatus(
                reporterUserId, request.targetType(), request.targetId(), ReportStatus.OPEN)) {
            throw new ContentException(ContentErrorCode.REPORT_ALREADY_OPEN);
        }

        try {
            ContentReport report = reportRepository.save(new ContentReport(request, reporterUserId));
            return ReportResponse.from(report);
        } catch (DataIntegrityViolationException e) {
            throw new ContentException(ContentErrorCode.REPORT_ALREADY_OPEN);
        }
    }

    /** Admin queue, oldest first; status null = OPEN. cursor = id of the last report received (null -> 0). */
    @Transactional(readOnly = true)
    public CursorPage<ReportResponse> queue(ReportStatus status, Long cursor, int size) {

        int limit = Math.max(1, Math.min(size, MAX_QUEUE_PAGE));
        ReportStatus filter = status == null ? ReportStatus.OPEN : status;

        List<ContentReport> fetched = reportRepository.findByStatusAndIdGreaterThanOrderByIdAsc(
                filter, cursor == null ? 0L : cursor, PageRequest.ofSize(limit + 1));

        boolean hasMore = fetched.size() > limit;
        List<ContentReport> page = hasMore ? fetched.subList(0, limit) : fetched;

        List<ReportResponse> responses = page.stream().map(ReportResponse::from).toList();
        Long nextCursor = hasMore ? page.getLast().getId() : null;

        return new CursorPage<>(responses, nextCursor);
    }

    /**
     * ONE transaction: the report must be OPEN; it becomes RESOLVED. With takeDown the reported track or album is taken down.
     * The OTHER open reports of the same target are closed as RESOLVED too: the same decision answers them.
     */
    @Transactional
    public ReportResponse resolve(Long reportId, Long adminId, String note, boolean takeDown) {

        ContentReport report = findOpen(reportId);
        report.resolve(adminId, note);

        if (takeDown) {
            switch (report.getTargetType()) {
                case TRACK -> adminCatalogService.takeDownTrack(report.getTargetId(), adminId);
                case ALBUM -> adminCatalogService.takeDownAlbum(report.getTargetId(), adminId);
                // there is no admin takedown for playlists
                default -> log.warn("takeDown ignored for report {}: {} cannot be taken down",
                        reportId, report.getTargetType());
            }
        }

        for (ContentReport other : reportRepository.findByTargetTypeAndTargetIdAndStatus(
                report.getTargetType(), report.getTargetId(), ReportStatus.OPEN)) {
            if (!other.getId().equals(report.getId())) {
                other.resolve(adminId, note);
            }
        }

        // managed entities inside a transaction: dirty checking flushes every change on commit
        return ReportResponse.from(report);
    }

    /** OPEN -> DISMISSED with the note; REPORT_ALREADY_CLOSED otherwise. */
    @Transactional
    public ReportResponse dismiss(Long reportId, Long adminId, String note) {

        ContentReport report = findOpen(reportId);
        report.dismiss(adminId, note);
        return ReportResponse.from(report);
    }

    // ---------------------------------------------------------------------------------------------------------------

    private ContentReport findOpen(Long reportId) {
        ContentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() != ReportStatus.OPEN) {
            throw new ContentException(ContentErrorCode.REPORT_ALREADY_CLOSED);
        }
        return report;
    }

    /** Who owns the target (a user id) and whether an ordinary viewer can see it. Throws *_NOT_FOUND when it does not exist. */
    private Target loadTarget(ReportTargetType type, Long targetId) {
        return switch (type) {
            case TRACK -> {
                Track track = trackRepository.findByIdAndIsDeleted(targetId, NOT_DELETED)
                        .orElseThrow(() -> notFound(type));
                yield new Target(ownerOfArtist(track.getArtistId()), track.getStatus() == ContentStatus.PUBLISHED);
            }
            case ALBUM -> {
                Album album = albumRepository.findByIdAndIsDeleted(targetId, NOT_DELETED)
                        .orElseThrow(() -> notFound(type));
                yield new Target(ownerOfArtist(album.getArtistId()), album.getStatus() == ContentStatus.PUBLISHED);
            }
            case PLAYLIST -> {
                Playlist playlist = playlistRepository.findByIdAndIsDeleted(targetId, NOT_DELETED)
                        .orElseThrow(() -> notFound(type));
                yield new Target(playlist.getOwnerUserId(), playlist.getVisibility() == PlaylistVisibility.PUBLIC);
            }
        };
    }

    /** Track.artistId and Album.artistId are artists.id; ownership is decided on the artist's owner user. */
    private Long ownerOfArtist(Long artistId) {
        return artistRepository.findById(artistId).map(Artist::getOwnerUserId).orElse(null);
    }

    private static ContentException notFound(ReportTargetType type) {
        return new ContentException(switch (type) {
            case TRACK -> ContentErrorCode.TRACK_NOT_FOUND;
            case ALBUM -> ContentErrorCode.ALBUM_NOT_FOUND;
            case PLAYLIST -> ContentErrorCode.PLAYLIST_NOT_FOUND;
        });
    }

    private record Target(Long ownerUserId, boolean visible) {}
}