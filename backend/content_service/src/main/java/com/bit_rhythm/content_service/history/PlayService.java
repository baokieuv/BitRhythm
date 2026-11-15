package com.bit_rhythm.content_service.history;

import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

@Service
public class PlayService {

    /**
     * [S1] TODO(hint): the client reports a play after content.playback.min-play-seconds of listening.
     *  1. positionSeconds below that threshold -> ignore silently (not an error);
     *  2. the track must exist and be PUBLISHED (TRACK_NOT_FOUND);
     *  3. PlayCounter.markFirstPlayInWindow(userId, trackId) false -> repeat of the same play: do nothing;
     *  4. PlayHistory row (userId, trackId) + PlayCounter.increment(trackId).
     * The recently-played list does not need the counter: the history row is written every time step 3 lets it through.
     */
    public void recordPlay(Long userId, Long trackId, int positionSeconds) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): recently played, newest first (cursor = id of the PlayHistory row). The same track played several times appears once
     * per play: collapse neighbours of the same track if the UI wants a clean list. Tracks no longer PUBLISHED are left out.
     */
    public CursorPage<TrackResponse> history(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }
}
