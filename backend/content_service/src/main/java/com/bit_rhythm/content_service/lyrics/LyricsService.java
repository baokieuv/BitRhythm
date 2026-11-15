package com.bit_rhythm.content_service.lyrics;

import com.bit_rhythm.content_service.lyrics.dto.LyricsRequest;
import com.bit_rhythm.content_service.lyrics.dto.LyricsResponse;
import org.springframework.stereotype.Service;

@Service
public class LyricsService {

    /** [S2] TODO(hint): same visibility as the track (TRACK_NOT_FOUND for a draft of somebody else). A track without lyrics answers an EMPTY response (null texts, no credits), not a 404. */
    public LyricsResponse get(Long trackId, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint), ONE transaction: the track must belong to the artist of userId (TRACK_NOT_FOUND). Upsert the TrackLyrics row, replace the
     * credits (CreditRepository.deleteAllOfTrack, then insert the new ones). Validate syncedLrc loosely (every non-empty line starts with
     * "[mm:ss"): refuse obvious garbage, do not try to parse every dialect of LRC.
     */
    public LyricsResponse save(Long userId, Long trackId, LyricsRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
