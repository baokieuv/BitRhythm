package com.bit_rhythm.content_service.common.exception;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentErrorCodeTest {

    @Test
    void everyCodeIsUniquePrefixedAndMapsToAnErrorStatus() {
        Set<String> seen = new HashSet<>();
        for (ContentErrorCode code : ContentErrorCode.values()) {
            assertTrue(code.getCode().startsWith("CONTENT_"), code.name());
            assertTrue(seen.add(code.getCode()), "duplicate code string " + code.getCode());
            assertTrue(code.getHttpStatus().isError(), code.name());
        }
    }

    @Test
    void exceptionKeepsTheCodeAndAddsTheDetailOnlyToTheLogMessage() {
        ContentException ex = new ContentException(ContentErrorCode.TRACK_NOT_FOUND, "id 42");

        assertEquals(ContentErrorCode.TRACK_NOT_FOUND, ex.getErrorCode());
        assertEquals(ContentErrorCode.TRACK_NOT_FOUND.getMessage() + ": id 42", ex.getMessage());
    }
}
