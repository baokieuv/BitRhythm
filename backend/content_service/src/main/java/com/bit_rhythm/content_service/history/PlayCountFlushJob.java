package com.bit_rhythm.content_service.history;

import com.bit_rhythm.content_service.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Moves the play counters from Redis into tracks.play_count. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayCountFlushJob {

    private final PlayCounter playCounter;
    private final TrackRepository trackRepository;

    /**
     * Drain the counters, then one atomic UPDATE per track (addPlays). A failing track never stops the others: its plays are given
     * back to Redis for the next run, and if that fails too they are lost (acceptable for statistics). Several instances can run
     * this at once: GETDEL in drain() moves each play exactly once.
     */
    @Scheduled(fixedDelayString = "${content.playback.flush-interval:30s}")
    public void flush() {

        Map<Long, Long> drained;
        try {
            drained = playCounter.drain();
        } catch (Exception e) {
            log.error("play counter drain failed: {}", e.toString());
            return;
        }

        if (drained.isEmpty()) {
            return;
        }

        int failed = 0;
        for (Map.Entry<Long, Long> entry : drained.entrySet()) {
            try {
                trackRepository.addPlays(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                failed++;
                log.error("addPlays failed for track {} ({} plays): {}", entry.getKey(), entry.getValue(), e.toString());
                try {
                    playCounter.add(entry.getKey(), entry.getValue());
                } catch (Exception ignored) {
                    log.error("{} plays of track {} lost", entry.getValue(), entry.getKey());
                }
            }
        }

        log.debug("flushed play counts of {} tracks ({} failed)", drained.size(), failed);
    }
}