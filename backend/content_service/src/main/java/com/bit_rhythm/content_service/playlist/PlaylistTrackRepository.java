package com.bit_rhythm.content_service.playlist;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {

    List<PlaylistTrack> findByPlaylistIdOrderByPositionAsc(Long playlistId);   // reorder: the whole playlist (max content.playlist.max-tracks)

    /** Pagination by position: cursor = position of the last row received (0 for the first page). Ask for size + 1 rows. */
    List<PlaylistTrack> findByPlaylistIdAndPositionGreaterThanOrderByPositionAsc(Long playlistId, int position, Pageable pageable);

    boolean existsByPlaylistIdAndTrackId(Long playlistId, Long trackId);

    @Query("SELECT COALESCE(MAX(pt.position), 0) FROM PlaylistTrack pt WHERE pt.playlistId = :playlistId")
    int maxPosition(@Param("playlistId") Long playlistId);

    /** Number of rows deleted (0 or 1): adjust playlist.trackCount ONLY when it is 1. */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlaylistTrack pt WHERE pt.playlistId = :playlistId AND pt.trackId = :trackId")
    int deletePair(@Param("playlistId") Long playlistId, @Param("trackId") Long trackId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlaylistTrack pt WHERE pt.playlistId = :playlistId")
    int deleteAllOfPlaylist(@Param("playlistId") Long playlistId);

    /** A track was deleted: remove it from every playlist (and correct the counters of those playlists first). */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PlaylistTrack pt WHERE pt.trackId = :trackId")
    int deleteAllOfTrack(@Param("trackId") Long trackId);

    List<PlaylistTrack> findByTrackId(Long trackId);
}
