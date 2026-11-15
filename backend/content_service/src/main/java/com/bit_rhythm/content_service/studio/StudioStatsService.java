package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.studio.dto.StudioStatsResponse;
import org.springframework.stereotype.Service;

@Service
public class StudioStatsService {

    /**
     * [S1] TODO(hint): counts with TrackRepository.countByArtistId / countByArtistIdAndStatus; totalPlays / totalLikes with ONE aggregate
     * query (SUM over the artist's tracks, add it to TrackRepository); topTracks = the 5 most played (a Pageable of 5, ordered by playCount).
     * Plays are flushed from Redis to playCount every few seconds, so the numbers can lag a little: say so in the API docs.
     */
    public StudioStatsResponse stats(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }
}
