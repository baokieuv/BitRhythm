package com.bit_rhythm.notification_service.inbox;

import com.bit_rhythm.notification_service.config.NotificationProperties;
import com.bit_rhythm.notification_service.core.NotificationType;
import com.bit_rhythm.notification_service.inbox.realtime.RealtimeBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxRepository inboxRepository;
    private final RealtimeBroadcaster realtimeBroadcaster;
    private final NotificationProperties properties;

    @Transactional
    public InboxNotification create(Long userId, NotificationType type, String title, String body) {
        InboxNotification notification = new InboxNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        InboxNotification saved = inboxRepository.save(notification);
        // Only a hint ("something new"): the client then refreshes the list / unread count.
        realtimeBroadcaster.publish(userId, saved.getId(), type);
        return saved;
    }

    /** @param cursor id of the last item already received (null = first page) */
    @Transactional(readOnly = true)
    public List<InboxResponse> list(Long userId, Long cursor, int size, boolean unreadOnly) {
        int pageSize = Math.max(1, Math.min(size, properties.inbox().maxPageSize()));
        long before = cursor == null ? Long.MAX_VALUE : cursor;
        PageRequest page = PageRequest.of(0, pageSize);
        List<InboxNotification> response = unreadOnly
                ? inboxRepository.findByUserIdAndReadAtIsNullAndIdLessThanOrderByIdDesc(userId, before, page)
                : inboxRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, before, page);

        return response.stream().map(InboxResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return inboxRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        inboxRepository.markRead(id, userId, Instant.now());
    }

    @Transactional
    public void markAllRead(Long userId) {
        inboxRepository.markAllRead(userId, Instant.now());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        inboxRepository.deleteOwned(id, userId);
    }
}
