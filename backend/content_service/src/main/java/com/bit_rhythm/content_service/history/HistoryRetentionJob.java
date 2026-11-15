package com.bit_rhythm.content_service.history;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** [S2] The play history grows with every listen: keep only what the product needs. */
@Component
public class HistoryRetentionJob {

    /**
     * [S2] TODO(hint): PlayHistoryRepository.deleteOlderThan(now - N days) (e.g. 180 days, add content.history.retention to the properties).
     * A big table: delete in batches (a LIMIT-ed DELETE in a loop) so the job does not hold locks for minutes. A no-op until implemented.
     */
    @Scheduled(cron = "${content.history.retention-cron:0 30 3 * * *}")
    public void purge() {
        // TODO
    }
}
