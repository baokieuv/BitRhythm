package com.bit_rhythm.notification_service.delivery;

import com.bit_rhythm.notification_service.core.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * NotificationRequest <-> JSON, for notification_log.payload.
 * Uses Jackson 3 (Spring Boot 4: package tools.jackson). On Jackson 2 the import is com.fasterxml.jackson.databind
 * and the two calls throw a checked JsonProcessingException that must be caught.
 */
@Component
@RequiredArgsConstructor
public class RequestPayloadCodec {

    private final ObjectMapper objectMapper;

    public String write(NotificationRequest request) {
        return objectMapper.writeValueAsString(request);
    }

    public NotificationRequest read(String json) {
        return objectMapper.readValue(json, NotificationRequest.class);
    }
}
