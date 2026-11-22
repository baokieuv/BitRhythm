package com.bit_rhythm.content_service.block;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final BlockService blockService;
    private final ObjectMapper objectMapper;

    public void onEvent(String json) {
        BlockEvent event;

        try {
            event = objectMapper.readValue(json, BlockEvent.class);
        } catch (Exception e) {
            // no payload in the log: it holds email addresses
            log.error("Ignoring an auth event that is not valid JSON ({} characters): {}",
                    json == null ? 0 : json.length(), e.getClass().getSimpleName());
            return;
        }

        if (event == null || isBlank(event.eventId()) || isBlank(event.eventType())) {
            log.error("Ignoring an auth event without eventId, eventType");
            return;
        }

        blockService.handleEvent(event);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
