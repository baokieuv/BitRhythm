package com.bit_rhythm.content_service.lyrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LyricsRepository extends JpaRepository<TrackLyrics, Long> {
    Optional<TrackLyrics> findByTrackId(Long trackId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TrackLyrics l WHERE l.trackId = :trackId")
    int deleteByTrackId(@Param("trackId") Long trackId);   // [S2] track deleted: delete all lyrics of the track
}
