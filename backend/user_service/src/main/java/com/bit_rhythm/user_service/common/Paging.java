package com.bit_rhythm.user_service.common;

/** Small helpers shared by the cursor-paginated lists. */
public final class Paging {

    private Paging() {
    }

    /** At least 1, at most the configured maximum (user.pagination.max-page-size). */
    public static int clampSize(int requested, int max) {
        return Math.max(1, Math.min(requested, max));
    }

    /** First page of a list sorted by id DESC: nothing is "after" Long.MAX_VALUE. */
    public static long startCursor(Long cursor) {
        return cursor == null ? Long.MAX_VALUE : cursor;
    }
}

