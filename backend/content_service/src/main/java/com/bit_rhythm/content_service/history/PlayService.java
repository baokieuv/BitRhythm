package com.bit_rhythm.content_service.history;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayService {

    private static final int NOT_DELETED = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

    private final TrackRepository trackRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final PlayCounter playCounter;
    private final TrackService trackService;
    private final ContentProperties properties;

    /**
     * The client reports a play after content.playback.min-play-seconds of listening.
     *  1. below that threshold: ignored silently (not an error);
     *  2. the track must exist and be PUBLISHED (TRACK_NOT_FOUND);
     *  3. a repeat of the same play inside the window: nothing happens;
     *  4. a PlayHistory row and a +1 in the Redis counter.
     * Not @Transactional: the history insert is its own transaction, and Redis is outside any transaction anyway.
     */
    public void recordPlay(Long userId, Long trackId, int positionSeconds) {

        if (positionSeconds < properties.playback().minPlaySeconds()) {
            return;
        }

        trackRepository.findByIdAndIsDeleted(trackId, NOT_DELETED)
                .filter(track -> track.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        try {
            if (!playCounter.markFirstPlayInWindow(userId, trackId)) {
                return;
            }
        } catch (Exception e) {
            // Redis is down: without the anti-spam marker we cannot tell a repeat from a real play, so count nothing
            // (a statistic is not worth a 500, and counting blindly would let a loop inflate the track)
            log.warn("play of track {} not counted, Redis unavailable: {}", trackId, e.toString());
            return;
        }

        playHistoryRepository.save(new PlayHistory(userId, trackId));

        try {
            playCounter.increment(trackId);
        } catch (Exception e) {
            log.warn("play counter of track {} not incremented: {}", trackId, e.toString());
        }
    }

    /**
     * Recently played, newest first. cursor = id of the PlayHistory row. Tracks no longer PUBLISHED are left out (the page can be
     * shorter than size: the cursor still moves past them). The same track played twice in a row appears once.
     */
    public CursorPage<TrackResponse> history(Long userId, Long cursor, int size) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<PlayHistory> fetched = playHistoryRepository.findByUserIdAndIdLessThanOrderByIdDesc(
                userId, Paging.startCursor(cursor), PageRequest.ofSize(limit + 1));

        boolean hasMore = fetched.size() > limit;
        List<PlayHistory> rows = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? rows.getLast().getId() : null;

        if (rows.isEmpty()) {
            return new CursorPage<>(List.of(), nextCursor);
        }

        // one query for the tracks of the page
        Map<Long, Track> byId = trackRepository
                .findByIdInAndIsDeleted(rows.stream().map(PlayHistory::getTrackId).distinct().toList(), NOT_DELETED).stream()
                .collect(Collectors.toMap(Track::getId, Function.identity(), (a, b) -> a));

        List<Track> ordered = new ArrayList<>(rows.size());
        Long previousTrackId = null;
        for (PlayHistory row : rows) {
            Track track = byId.get(row.getTrackId());
            if (track == null || track.getStatus() != ContentStatus.PUBLISHED) {
                continue;
            }
            if (row.getTrackId().equals(previousTrackId)) {
                continue;   // same track replayed right away: one entry
            }
            ordered.add(track);
            previousTrackId = row.getTrackId();
        }

        return new CursorPage<>(trackService.toResponses(ordered, userId), nextCursor);
    }
}