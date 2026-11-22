package com.bit_rhythm.content_service.lyrics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LyricsRepository extends JpaRepository<TrackLyrics, Long> {
    Optional<TrackLyrics> findByTrackId(Long trackId);
}
