package com.bit_rhythm.content_service.history;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Play counters in Redis: a play only does INCR here (fast, no database write); PlayCountFlushJob moves the totals into
 * tracks.play_count in batches. Keys: plays:pending:{trackId} (counter) and plays:seen:{userId}:{trackId} (anti-spam marker).
 */
@Component
@RequiredArgsConstructor
public class PlayCounter {

    private final StringRedisTemplate redis;

    /**
     * TODO(hint): "has this user already been counted for this track recently?" Set plays:seen:{userId}:{trackId} with
     * opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10)): true = first play in the window (count it), false = repeat (do not count).
     * Stops a client from inflating a track by calling the endpoint in a loop.
     */
    public boolean markFirstPlayInWindow(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** TODO(hint): opsForValue().increment("plays:pending:" + trackId). */
    public void increment(Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * TODO(hint): returns {trackId -> plays since the last drain} and resets them. Scan the keys plays:pending:* with a cursor-based SCAN
     * (never KEYS in production) and read each one with getAndDelete (atomic: a play that arrives meanwhile is kept for the next drain).
     * If the database write fails afterwards the plays of that drain are lost: acceptable for statistics, say so in the docs.
     */
    public Map<Long, Long> drain() {
        throw new UnsupportedOperationException("TODO");
    }
}
