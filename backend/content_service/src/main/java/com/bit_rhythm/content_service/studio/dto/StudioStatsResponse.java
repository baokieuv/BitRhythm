package com.bit_rhythm.content_service.studio.dto;

import java.util.List;

public record StudioStatsResponse(
        long tracks,
        long publishedTracks,
        long albums,
        long totalPlays,
        long totalLikes,
        List<TrackStat> topTracks
) {
    public record TrackStat(Long trackId, String title, long plays, long likes) {
    }
}
