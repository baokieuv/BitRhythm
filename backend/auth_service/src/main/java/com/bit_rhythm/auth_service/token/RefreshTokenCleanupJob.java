package com.bit_rhythm.auth_service.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {
    private static final Duration RETENTION = Duration.ofDays(7);

    private RefreshTokenService refreshTokenService;

    @Scheduled(cron = "${auth.token.cleanup-cron:0 0 0 * * *}") // Default: every day at midnight
    public void purgeExpiredRefreshTokens() {
        log.info("Starting purge of expired refresh tokens older than {} days", RETENTION.toDays());
        int deletedCount = refreshTokenService.purgeExpired(RETENTION);
        log.info("Purge completed. Deleted {} expired refresh tokens.", deletedCount);
    }
}
