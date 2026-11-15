package com.bit_rhythm.content_service.lyrics.dto;

import java.util.List;

public record LyricsResponse(Long trackId, String plainText, String syncedLrc, List<CreditDto> credits) {
}
