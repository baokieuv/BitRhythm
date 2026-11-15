package com.bit_rhythm.content_service.history;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Moves the play counters from Redis into tracks.play_count. */
@Component
public class PlayCountFlushJob {

    /**
     * [S1] TODO(hint): PlayCounter.drain(), then for each entry TrackRepository.addPlays(trackId, plays) (one atomic UPDATE each, or group
     * them in a transaction). A failing track must not stop the others: log it and go on. Several instances can run it: getAndDelete in
     * drain() guarantees that each play is moved exactly once. The method is a no-op until you implement it.
     */
    @Scheduled(fixedDelayString = "${content.playback.flush-interval:30s}")
    public void flush() {
        // TODO
    }
}
