package com.bit_rhythm.content_service.library;

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
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The personal library of a user: liked tracks and saved albums.
 *
 * Methods that race on a unique key (likeTrack, saveAlbum) are NOT @Transactional on purpose (design doc, section 12): the write
 * runs in its own transaction and the DataIntegrityViolationException is caught outside it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private static final int NOT_DELETED = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

    private final TrackLikeRepository trackLikeRepository;
    private final SavedAlbumRepository savedAlbumRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final TrackService trackService;
    private final AlbumService albumService;
    private final TransactionTemplate transactionTemplate;
    private final ContentProperties properties;

    // ------------------------------------------------------------------------------------------------------ tracks

    /** Like a track. Idempotent; the like row and the counter change in ONE transaction. */
    public void likeTrack(Long userId, Long trackId) {

        trackRepository.findById(trackId)
                .filter(track -> track.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        if (trackLikeRepository.existsByUserIdAndTrackId(userId, trackId)) {
            return;
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                trackLikeRepository.saveAndFlush(new TrackLike(userId, trackId));   // a duplicate fails HERE, before the counter
                trackRepository.addLikes(trackId, 1);
            });
        } catch (DataIntegrityViolationException e) {
            // a concurrent request liked it first: its transaction counted the like, ours rolled back completely
            log.debug("duplicate like ignored: user={} track={}", userId, trackId);
        }
    }

    /**
     * Remove a like. Idempotent. Works even if the track was unpublished since: the counter must still go down.
     * The delete and the counter are ONE transaction, and the counter changes only when a row was really deleted.
     */
    public void unlikeTrack(Long userId, Long trackId) {
        transactionTemplate.executeWithoutResult(status -> {
            if (trackLikeRepository.deletePair(userId, trackId) == 1) {
                trackRepository.addLikes(trackId, -1);
            }
        });
    }

    /**
     * The liked tracks, most recently liked first. cursor = id of the TrackLike row (not of the track).
     * Tracks that are no longer PUBLISHED are left out, so a page can be shorter than size; the cursor still moves past them.
     */
    public CursorPage<TrackResponse> likedTracks(Long userId, Long cursor, int size) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<TrackLike> fetched = trackLikeRepository.findByUserIdAndIdLessThanOrderByIdDesc(
                userId, Paging.startCursor(cursor), PageRequest.ofSize(limit + 1));

        boolean hasMore = fetched.size() > limit;
        List<TrackLike> rows = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? rows.getLast().getId() : null;

        if (rows.isEmpty()) {
            return new CursorPage<>(List.of(), nextCursor);
        }

        // one query for all tracks of the page, then restore the "most recently liked first" order
        Map<Long, Track> byId = trackRepository
                .findByIdInAndIsDeleted(rows.stream().map(TrackLike::getTrackId).toList(), NOT_DELETED).stream()
                .collect(Collectors.toMap(Track::getId, Function.identity(), (a, b) -> a));

        List<Track> ordered = rows.stream()
                .map(like -> byId.get(like.getTrackId()))
                .filter(Objects::nonNull)
                .filter(track -> track.getStatus() == ContentStatus.PUBLISHED)
                .toList();

        return new CursorPage<>(trackService.toResponses(ordered, userId), nextCursor);
    }

    /** Which of these tracks the user has liked. */
    public Set<Long> likedAmong(Long userId, Collection<Long> trackIds) {
        if (userId == null || trackIds == null || trackIds.isEmpty()) {
            return Collections.emptySet();
        }
        return trackLikeRepository.findByUserIdAndTrackIdIn(userId, trackIds).stream()
                .map(TrackLike::getTrackId)
                .collect(Collectors.toSet());
    }

    // ------------------------------------------------------------------------------------------------------ albums

    /** Save an album. Idempotent; the unique key settles concurrent requests (no counter here). */
    public void saveAlbum(Long userId, Long albumId) {

        albumRepository.findById(albumId)
                .filter(album -> album.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ContentException(ContentErrorCode.ALBUM_NOT_FOUND));

        if (savedAlbumRepository.existsByUserIdAndAlbumId(userId, albumId)) {
            return;
        }

        try {
            savedAlbumRepository.saveAndFlush(new SavedAlbum(userId, albumId));
        } catch (DataIntegrityViolationException e) {
            log.debug("duplicate save ignored: user={} album={}", userId, albumId);
        }
    }

    /** Remove a saved album. Idempotent. */
    public void unsaveAlbum(Long userId, Long albumId) {
        savedAlbumRepository.deletePair(userId, albumId);
    }

    /** The saved albums, most recently saved first. cursor = id of the SavedAlbum row. Albums no longer PUBLISHED are left out. */
    public CursorPage<AlbumResponse> savedAlbums(Long userId, Long cursor, int size) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<SavedAlbum> fetched = savedAlbumRepository.findByUserIdAndIdLessThanOrderByIdDesc(
                userId, Paging.startCursor(cursor), PageRequest.ofSize(limit + 1));

        boolean hasMore = fetched.size() > limit;
        List<SavedAlbum> rows = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? rows.getLast().getId() : null;

        if (rows.isEmpty()) {
            return new CursorPage<>(List.of(), nextCursor);
        }

        Map<Long, Album> byId = albumRepository
                .findByIdIn(rows.stream().map(SavedAlbum::getAlbumId).toList()).stream()
                .collect(Collectors.toMap(Album::getId, Function.identity(), (a, b) -> a));

        List<Album> ordered = rows.stream()
                .map(saved -> byId.get(saved.getAlbumId()))
                .filter(Objects::nonNull)
                .filter(album -> album.getStatus() == ContentStatus.PUBLISHED)
                .toList();

        return new CursorPage<>(albumService.toResponses(ordered, userId), nextCursor);
    }

    /** Which of these albums the user has saved. */
    public Set<Long> savedAmong(Long userId, Collection<Long> albumIds) {
        if (userId == null || albumIds == null || albumIds.isEmpty()) {
            return Collections.emptySet();
        }
        return savedAlbumRepository.findByUserIdAndAlbumIdIn(userId, albumIds).stream()
                .map(SavedAlbum::getAlbumId)
                .collect(Collectors.toSet());
    }
}