package com.bit_rhythm.notification_service.template;

import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.core.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findFirstByTypeAndChannelAndLocaleAndActiveTrueOrderByVersionDesc(
            NotificationType type, Channel channel, String locale);

    List<NotificationTemplate> findAllByIsDeleted(Integer isDeleted);
}
