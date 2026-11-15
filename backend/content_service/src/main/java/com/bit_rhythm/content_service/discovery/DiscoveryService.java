package com.bit_rhythm.content_service.discovery;

import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/** [S2] Simple, explainable recommendations: no machine learning needed. */
@Service
public class DiscoveryService {

    /** [S2] TODO(hint): the most played PUBLISHED tracks of all time (TrackRepository.findByStatusOrderByPlayCountDescIdDesc with PageRequest.of(0, size)); cache the result in Redis for a few minutes. */
    public List<TrackResponse> popular(int size, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint): what is rising now. TrendingJob keeps a Redis sorted set (plays of the last 7 days as the score); read the top N with
     * opsForZSet().reverseRange, load the tracks in ONE query and keep the order of the set; fall back to popular() when the set is empty.
     */
    public List<TrackResponse> trending(int size, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint): "because you liked": take the genres and artists of the user's last liked / played tracks, then the most played
     * PUBLISHED tracks of those genres and artists, minus what the user already liked. No data yet (new user) -> popular().
     */
    public List<TrackResponse> forYou(Long userId, int size) {
        throw new UnsupportedOperationException("TODO");
    }
}
