package com.bit_rhythm.content_service.lyrics.dto;

import com.bit_rhythm.content_service.lyrics.TrackCredit;
import com.bit_rhythm.content_service.lyrics.TrackLyrics;

import java.util.List;

public record LyricsResponse(Long trackId, String plainText, String syncedLrc, List<CreditDto> credits) {

    public static LyricsResponse from(TrackLyrics lyrics, List<TrackCredit> credits) {
        return new LyricsResponse(
                lyrics.getTrackId(),
                lyrics.getPlainText(),
                lyrics.getSyncedLrc(),
                credits.stream().map(CreditDto::from).toList()
        );
    }

    public static LyricsResponse empty() {
        return new LyricsResponse(null, null, null, null);
    }
}
