package com.bit_rhythm.noti_service.delivery;

import com.bit_rhythm.core.entity.BaseEntity;
import com.bit_rhythm.noti_service.channel.Channel;
import com.bit_rhythm.noti_service.core.NotificationType;

import java.time.LocalDateTime;

public class NotificationLog extends BaseEntity {
    private Long id;
    private Long eventId;
    private Long userId;
    private NotificationType type;
    private Channel channel;
    private Boolean recipientMasked;
    private Integer status;
    private Integer attempt;
    private String provider;
    private Long providerMessageId;
    private String errorCode;
    private LocalDateTime sentAt;
}
