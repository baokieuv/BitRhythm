package com.bit_rhythm.content_service.lyrics;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LyricsRepository extends JpaRepository<TrackLyrics, Long> {
}
