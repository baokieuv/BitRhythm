package com.bit_rhythm.content_service.admin;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.album.AlbumService;
import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.content_service.track.TrackService;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Moderation of the catalog (ROLE_ADMIN). A taken-down item is TAKEN_DOWN, a status the owner cannot change: the studio refuses to
 * publish it, and only restore*() moves it on (to HIDDEN; the owner decides whether to publish again).

 * take-down and restore are @Transactional so they join the transaction of ReportService.resolve when called from it: the report is
 * closed and the content is taken down together, or neither.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCatalogService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final TrackService trackService;
    private final AlbumService albumService;
    private final ContentProperties properties;


    /**
     * Tracks of every artist, newest first, keyset pagination by id; status null = every status. There is no "viewer" here (the
     * admin has no likes to show), so the liked flag is simply false.
     */
    @Transactional(readOnly = true)
    public CursorPage<TrackResponse> tracks(ContentStatus status, Long cursor, int size) {

        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());
        Pageable pageable = PageRequest.ofSize(limit + 1);
        long start = Paging.startCursor(cursor);

        List<Track> fetched = status == null
                ? trackRepository.findByIdLessThanOrderByIdDesc(start, pageable)
                : trackRepository.findByStatusAndIdLessThanOrderByIdDesc(status, start, pageable);

        boolean hasMore = fetched.size() > limit;
        List<Track> page = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? page.getLast().getId() : null;

        return new CursorPage<>(trackService.toResponses(page, null), nextCursor);
    }

    /**
     * Status TAKEN_DOWN (TRACK_NOT_FOUND if missing); idempotent. The track disappears from lists, search and streaming; playlists
     * keep their rows but skip it.
     */
    @Transactional
    public void takeDownTrack(Long trackId, Long adminId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        if (track.getStatus() == ContentStatus.TAKEN_DOWN) {
            return;
        }

        ContentStatus before = track.getStatus();
        track.setStatus(ContentStatus.TAKEN_DOWN);
        trackRepository.save(track);
        log.info("admin {} took down track {} (was {})", adminId, trackId, before);
    }

    /** TAKEN_DOWN -> HIDDEN, not straight to PUBLISHED: the owner decides to publish again. Any other status: nothing to do. */
    @Transactional
    public void restoreTrack(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        if (track.getStatus() == ContentStatus.TAKEN_DOWN) {
            track.setStatus(ContentStatus.HIDDEN);
            trackRepository.save(track);
            log.info("track {} restored to HIDDEN", trackId);
        }
    }

    /** Like tracks(). */
    @Transactional(readOnly = true)
    public CursorPage<AlbumResponse> albums(ContentStatus status, Long cursor, int size) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());
        Pageable pageable = PageRequest.ofSize(limit + 1);
        long start = Paging.startCursor(cursor);

        List<Album> fetched = status == null
                ? albumRepository.findByIdLessThanOrderByIdDesc(start, pageable)
                : albumRepository.findByStatusAndIdLessThanOrderByIdDesc(status, start, pageable);

        boolean hasMore = fetched.size() > limit;
        List<Album> page = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? page.getLast().getId() : null;

        return new CursorPage<>(albumService.toResponses(page, null), nextCursor);
    }

    /** Status TAKEN_DOWN; idempotent. The tracks of the album are NOT touched (take them down one by one if needed). */
    @Transactional
    public void takeDownAlbum(Long albumId, Long adminId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.ALBUM_NOT_FOUND));

        if (album.getStatus() == ContentStatus.TAKEN_DOWN) {
            return;
        }

        ContentStatus before = album.getStatus();
        album.setStatus(ContentStatus.TAKEN_DOWN);
        albumRepository.save(album);
        log.info("admin {} took down album {} (was {})", adminId, albumId, before);
    }

    /** TAKEN_DOWN -> HIDDEN. */
    @Transactional
    public void restoreAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.ALBUM_NOT_FOUND));

        if (album.getStatus() == ContentStatus.TAKEN_DOWN) {
            album.setStatus(ContentStatus.HIDDEN);
            albumRepository.save(album);
            log.info("album {} restored to HIDDEN", albumId);
        }
    }
}
