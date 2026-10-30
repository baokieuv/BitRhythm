package com.bit_rhythm.core.outbox;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class EventSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    // Converts an Object to a JSON String (Publisher Side)
    public static String serialize(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }

    // Converts a JSON String back to an Object (Consumer Side)
    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize outbox event payload to " + clazz.getSimpleName(), e);
        }
    }
}