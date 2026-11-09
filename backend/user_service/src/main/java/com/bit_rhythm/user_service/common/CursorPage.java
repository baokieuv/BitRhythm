package com.bit_rhythm.user_service.common;

import java.util.List;

public record CursorPage<T>(List<T> items, Long nextCursor) {}
