package com.bit_rhythm.content_service.studio.dto;

import com.bit_rhythm.content_service.track.dto.TrackResponse;

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

        public static TrackStat from(TrackResponse track) {
            return new TrackStat(track.id(), track.title(), track.playCount(), track.likeCount());
        }

    }
}
