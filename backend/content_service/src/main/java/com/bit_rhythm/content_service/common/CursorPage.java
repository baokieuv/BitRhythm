package com.bit_rhythm.content_service.common;

import java.util.List;

/**
 * Keyset pagination: send nextCursor back as ?cursor= to get the following page; null = no more pages.
 * Hint: read size + 1 rows; if you got the extra one there is a next page and the cursor is the id of the last row you return.
 */
public record CursorPage<T>(List<T> items, Long nextCursor) {
}
