package com.bit_rhythm.content_service.lyrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CreditRepository extends JpaRepository<TrackCredit, Long> {

    List<TrackCredit> findByTrackIdOrderByIdAsc(Long trackId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TrackCredit c WHERE c.trackId = :trackId")
    int deleteAllOfTrack(@Param("trackId") Long trackId);
}
