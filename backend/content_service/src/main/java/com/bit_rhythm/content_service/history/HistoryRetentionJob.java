package com.bit_rhythm.content_service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** The play history grows with every listen: keep only what the product needs. */
@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryRetentionJob {

    private static final int BATCH_SIZE = 5_000;
    /** Safety stop for one run (5,000,000 rows); the next night continues. */
    private static final int MAX_BATCHES = 1_000;

    private final PlayHistoryRepository playHistoryRepository;

    /** How long a play stays in the history, in days. */
    @Value("${content.history.retention-days:180}")
    private int retentionDays;

    /**
     * Delete plays older than the retention period in batches (a LIMIT-ed DELETE in a loop, each batch its own short transaction),
     * so the job never holds locks on the big table for minutes.
     */
    @Scheduled(cron = "${content.history.retention-cron:0 30 3 * * *}")
    public void purge() {

        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long total = 0;

        try {
            for (int batch = 0; batch < MAX_BATCHES; batch++) {
                int deleted = playHistoryRepository.deleteBatchOlderThan(threshold, BATCH_SIZE);
                total += deleted;
                if (deleted < BATCH_SIZE) {
                    break;
                }
            }
        } catch (Exception e) {
            // never let the scheduler thread die; what is left is deleted by the next run
            log.error("history purge stopped after {} rows: {}", total, e.toString());
            return;
        }

        log.info("history purge: {} rows older than {} days deleted", total, retentionDays);
    }
}