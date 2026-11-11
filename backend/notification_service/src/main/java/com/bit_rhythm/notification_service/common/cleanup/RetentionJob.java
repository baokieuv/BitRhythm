package com.bit_rhythm.notification_service.common.cleanup;

import com.bit_rhythm.notification_service.config.NotificationProperties;
import com.bit_rhythm.notification_service.delivery.NotificationLogRepository;
import com.bit_rhythm.notification_service.inbox.InboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetentionJob {

    private final NotificationLogRepository logRepository;
    private final InboxRepository inboxRepository;
    private final NotificationProperties properties;

    @Scheduled(cron = "${notification.retention.cron:0 30 3 * * *}")
    @Transactional
    public void purge() {
        Instant now = Instant.now();
        int logs = logRepository.deleteOlderThan(now.minus(properties.retention().log()));
        int inbox = inboxRepository.deleteOlderThan(now.minus(properties.retention().inbox()));
        log.info("Retention: deleted {} log rows and {} inbox items", logs, inbox);
    }
}