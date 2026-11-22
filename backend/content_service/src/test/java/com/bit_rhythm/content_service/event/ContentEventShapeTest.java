package com.bit_rhythm.content_service.event;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** The JSON of a TRACK_PUBLISHED event is a contract with its consumers (user-service): this test fails when the shape changes. Jackson 3 (tools.jackson). */
class ContentEventShapeTest {

    @Test
    void trackPublishedEventHasTheFieldsConsumersExpect() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("trackId", 123);
        data.put("title", "Song");
        data.put("artistId", 7);
        data.put("artistName", "Ann");
        data.put("artistUserId", 42);

        ContentEvent event = new ContentEvent("track-published:123", "TRACK_PUBLISHED", 1,
                Instant.parse("2026-11-08T10:15:30Z"), data, new ContentEvent.Metadata("content-service", null));

        String json = JsonMapper.builder().build().writeValueAsString(event);

        assertTrue(json.contains("\"eventId\":\"track-published:123\""), json);
        assertTrue(json.contains("\"eventType\":\"TRACK_PUBLISHED\""), json);
        assertTrue(json.contains("\"version\":1"), json);
        assertTrue(json.contains("\"data\":{\"trackId\":123,\"title\":\"Song\",\"artistId\":7,\"artistName\":\"Ann\",\"artistUserId\":42}"), json);
        assertTrue(json.contains("\"source\":\"content-service\""), json);
    }
}
