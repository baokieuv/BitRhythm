package com.bit_rhythm.content_service.discovery;

import com.bit_rhythm.content_service.history.PlayHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Rebuilds the Redis sorted set "trending:7d": member = track id, score = plays of the last 7 days. The set is built under a
 * NEW unique key and RENAMEd over the live one, so readers never see a half-built set. Several instances can run it at the same
 * time: each builds its own key, the result is the same, and the last rename wins.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrendingJob {

    static final String KEY = "trending:7d";

    private static final int WINDOW_DAYS = 7;
    /** Only the top of the ranking is ever read. */
    private static final int MAX_TRACKS = 200;
    /** Safety net: if the job stops running, the stale set expires and discovery falls back to popular(). */
    private static final Duration KEY_TTL = Duration.ofHours(1);

    private final PlayHistoryRepository playHistoryRepository;
    private final StringRedisTemplate redis;

    @Scheduled(fixedDelayString = "${content.discovery.trending-refresh:10m}")
    public void refresh() {

        String buildKey = KEY + ":build:" + UUID.randomUUID();
        try {
            Instant since = Instant.now().minus(WINDOW_DAYS, ChronoUnit.DAYS);
            List<PlayHistoryRepository.TrackPlayCount> counts =
                    playHistoryRepository.countPlaysSince(since, PageRequest.ofSize(MAX_TRACKS));

            if (counts.isEmpty()) {
                redis.delete(KEY);   // nothing is trending: readers fall back to popular()
                return;
            }

            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
            for (PlayHistoryRepository.TrackPlayCount count : counts) {
                tuples.add(ZSetOperations.TypedTuple.of(String.valueOf(count.getTrackId()), (double) count.getPlays()));
            }

            redis.opsForZSet().add(buildKey, tuples);
            redis.expire(buildKey, KEY_TTL);   // RENAME keeps the TTL, so it applies to the live key too
            redis.rename(buildKey, KEY);

            log.debug("trending refreshed: {} tracks", counts.size());
        } catch (Exception e) {
            // never let the scheduler thread die; the previous set stays in place (or expires after an hour)
            log.error("trending refresh failed: {}", e.toString());
            try {
                redis.delete(buildKey);
            } catch (Exception ignored) {
                // best effort
            }
        }
    }
}