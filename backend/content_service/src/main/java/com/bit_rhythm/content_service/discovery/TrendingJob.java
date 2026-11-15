package com.bit_rhythm.content_service.discovery;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrendingJob {

    /**
     * [S2] TODO(hint): rebuild the Redis sorted set "trending:7d": score = plays of the last 7 days per track (count PlayHistory rows with
     * playedAt in the window, GROUP BY track: add that query to PlayHistoryRepository), write it into a NEW key and RENAME it over the old one
     * so readers never see a half-built set. Running on several instances is harmless (same result). A no-op until implemented.
     */
    @Scheduled(fixedDelayString = "${content.discovery.trending-refresh:10m}")
    public void refresh() {
        // TODO
    }
}
