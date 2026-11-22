package com.bit_rhythm.content_service.common.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SqlLoader {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String load(String filename) {
        return cache.computeIfAbsent(filename, name -> {
            try {
                var resource = new ClassPathResource("sql/" + name);
                return resource.getContentAsString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to load SQL file: sql/" + name, e);
            }
        });
    }
}
