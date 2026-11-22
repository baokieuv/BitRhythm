package com.bit_rhythm.content_service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Play counters in Redis: a play only does INCR here (fast, no database write); PlayCountFlushJob moves the totals into
 * tracks.play_count in batches. Keys: plays:pending:{trackId} (counter) and plays:seen:{userId}:{trackId} (anti-spam marker).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayCounter {

    private static final String PENDING_PREFIX = "plays:pending:";
    private static final String SEEN_PREFIX = "plays:seen:";
    /** One user counts once per track in this window: a client cannot inflate a track by calling the endpoint in a loop. */
    private static final Duration SEEN_WINDOW = Duration.ofMinutes(10);
    private static final int SCAN_COUNT = 500;

    private final StringRedisTemplate redis;

    /**
     * true = first play of this track by this user in the window (count it); false = repeat (do not count).
     * SET ... NX EX is one atomic command, so two simultaneous requests cannot both win.
     */
    public boolean markFirstPlayInWindow(Long userId, Long trackId) {
        Boolean first = redis.opsForValue().setIfAbsent(SEEN_PREFIX + userId + ":" + trackId, "1", SEEN_WINDOW);
        return Boolean.TRUE.equals(first);
    }

    public void increment(Long trackId) {
        redis.opsForValue().increment(PENDING_PREFIX + trackId);
    }

    /** Put plays back after a failed database write, so they are moved on the next flush instead of being lost. */
    public void add(Long trackId, long plays) {
        redis.opsForValue().increment(PENDING_PREFIX + trackId, plays);
    }

    /**
     * Returns {trackId -> plays since the last drain} and resets them. The keys are found with a cursor-based SCAN (never KEYS on a
     * production Redis) and each one is read with GETDEL, which is atomic: a play that arrives meanwhile creates the key again and is
     * kept for the next drain, and two instances draining at once never take the same plays.
     * If the database write fails afterwards the caller can give them back with add(); if even that fails they are lost, which is
     * acceptable for statistics.
     */
    public Map<Long, Long> drain() {

        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(PENDING_PREFIX + "*").count(SCAN_COUNT).build();
        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }

        Map<Long, Long> totals = new HashMap<>();
        for (String key : keys) {
            try {
                String value = redis.opsForValue().getAndDelete(key);
                if (value == null) {
                    continue;   // another instance took it first
                }
                long trackId = Long.parseLong(key.substring(PENDING_PREFIX.length()));
                long plays = Long.parseLong(value);
                if (plays > 0) {
                    totals.merge(trackId, plays, Long::sum);
                }
            } catch (NumberFormatException e) {
                log.warn("ignoring malformed counter key {}", key);
            }
        }
        return totals;
    }
}