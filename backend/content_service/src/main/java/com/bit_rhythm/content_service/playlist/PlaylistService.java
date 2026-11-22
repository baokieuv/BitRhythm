package com.bit_rhythm.content_service.playlist;

import com.bit_rhythm.content_service.block.BlockRepository;
import com.bit_rhythm.content_service.common.Constant;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.common.utils.SqlLoader;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.playlist.dto.PlaylistRequest;
import com.bit_rhythm.content_service.playlist.dto.PlaylistResponse;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Playlists of the logged-in user. A playlist of somebody else is visible only when PUBLIC; every write needs ownership
 * (NOT_OWNER; for a PRIVATE playlist of somebody else answer PLAYLIST_NOT_FOUND instead, do not reveal it).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TrackRepository trackRepository;
    private final BlockRepository blockRepository;
    private final ContentProperties contentProperties;
    private final StorageClient storageClient;
    private final SqlLoader sqlLoader;

    /** Create a new playlist for the given user. */
    @Transactional
    public PlaylistResponse create(Long userId, PlaylistRequest request) {

        if (playlistRepository.countByOwnerUserId(userId) >= contentProperties.playlist().maxPlaylistsPerUser())
        {
            throw new ContentException(ContentErrorCode.PLAYLIST_LIMIT_REACHED);
        }

        Playlist playlist = new Playlist(request, userId);
        playlistRepository.save(playlist);
        return PlaylistResponse.from(playlist);
    }

    /** Get a playlist by its ID, if it exists and is accessible to the viewer. */
    @Transactional(readOnly = true)
    public PlaylistResponse get(Long playlistId, Long viewerId) {
        Playlist playlist = findActive(playlistId);
        assertVisible(playlist, viewerId);
        return PlaylistResponse.from(playlist);
    }

    /** Update a playlist by its ID, if it exists and is owned by the user. */
    @Transactional
    public PlaylistResponse update(Long userId, Long playlistId, PlaylistRequest request) {

        Playlist playlist = findOwned(userId, playlistId);

        playlist.update(request);
        // managed entity inside a transaction: dirty checking flushes it, no explicit save() needed
        return PlaylistResponse.from(playlist);
    }

    /** Delete a playlist by its ID, if it exists and is owned by the user. */
    @Transactional
    public void delete(Long userId, Long playlistId) {
        Playlist playlist = findOwned(userId, playlistId);

        playlistRepository.delete(playlist);
        playlistTrackRepository.deleteAllOfPlaylist(playlistId);
    }

    /** Get the playlists of the user, newest first, cursor pagination. */
    @Transactional(readOnly = true)
    public CursorPage<PlaylistResponse> mine(Long userId, Long cursor, int size) {
        int limit = Paging.clampSize(size, contentProperties.pagination().maxPageSize());

        List<Playlist> fetched = playlistRepository.findByOwnerUserIdOrderByCreatedAtDesc(
                userId, Paging.startCursor(cursor), PageRequest.ofSize(limit + 1));

        boolean hasMore = fetched.size() > limit;
        List<Playlist> playlists = hasMore ? fetched.subList(0, limit) : fetched;

        List<PlaylistResponse> responses = playlists.stream()
                .map(PlaylistResponse::from)
                .toList();

        Long nextCursor = hasMore ? responses.getLast().id() : null;
        return new CursorPage<>(responses, nextCursor);
    }

    /** Get the tracks in a playlist, ordered by position. */
    public CursorPage<TrackResponse> tracks(Long playlistId, Long viewerId, Long cursor, int size) {
        int limit = Paging.clampSize(size, contentProperties.pagination().maxPageSize());

        Playlist playlist = findActive(playlistId);
        assertVisible(playlist, viewerId);

        long startCursor = (cursor == null) ? 0L : cursor;
        String sql = sqlLoader.load(Constant.SqlPath.GET_PLAYLIST_TRACKS);
        String coverBucket = contentProperties.cover().bucket();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("playlistId", playlist.getId())
                .addValue("viewerId", viewerId, Types.BIGINT)
                .addValue("cursor", startCursor)
                .addValue("limit", limit + 1);

        List<PositionedTrack> fetched = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String coverUrl = storageClient.publicUrl(coverBucket, rs.getString("cover_key"));

            TrackResponse track = new TrackResponse(rs, coverUrl);
            int position = rs.getInt("position");

            return new PositionedTrack(position, track);
        });

        boolean hasMore = fetched.size() > limit;
        List<PositionedTrack> pageItems = hasMore ? fetched.subList(0, limit) : fetched;

        List<TrackResponse> responses = pageItems.stream()
                .map(PositionedTrack::track)
                .toList();

        Long nextCursor = hasMore ? (long) pageItems.getLast().position() : null;
        return new CursorPage<>(responses, nextCursor);
    }

    /** Add a track to a playlist. */
    @Transactional
    public void addTrack(Long userId, Long playlistId, Long trackId) {

        Playlist playlist = findOwned(userId, playlistId);

        if (playlist.getTrackCount() >= contentProperties.playlist().maxTracks()) {
            throw new ContentException(ContentErrorCode.PLAYLIST_FULL);
        }

        Track track = trackRepository.findById(trackId)
                .filter(t -> t.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        if (blockRepository.existsByBlockerIdAndBlockedId(userId, track.getArtistId())) {
            throw new ContentException(ContentErrorCode.TRACK_NOT_FOUND);
        }

        if (playlistTrackRepository.existsByPlaylistIdAndTrackId(playlistId, trackId)) {
            throw new ContentException(ContentErrorCode.TRACK_ALREADY_IN_PLAYLIST);
        }

        int nextPosition = playlistTrackRepository.maxPosition(playlistId) + 1;
        playlistTrackRepository.save(new PlaylistTrack(playlistId, trackId, nextPosition));
        playlistRepository.addTracks(playlistId, 1);
    }

    /** Remove a track from a playlist. */
    @Transactional
    public void removeTrack(Long userId, Long playlistId, Long trackId) {

        findOwned(userId, playlistId);

        if (playlistTrackRepository.deletePair(playlistId, trackId) > 0) {
            playlistRepository.addTracks(playlistId, -1);
        }
    }

    /** Reorder the tracks in a playlist. */
    public void reorder(Long userId, Long playlistId, List<Long> trackIds) {
        findOwned(userId, playlistId);

        List<PlaylistTrack> existing = playlistTrackRepository.findByPlaylistIdOrderByPositionAsc(playlistId);

        if (trackIds == null || trackIds.size() != existing.size()) {
            throw new ContentException(ContentErrorCode.INVALID_TRACK_ORDER);
        }

        Map<Long, PlaylistTrack> byTrackId = new HashMap<>(existing.size() * 2);
        for (PlaylistTrack pt : existing) {
            byTrackId.put(pt.getTrackId(), pt);
        }

        // single pass: every requested id must exist and appear exactly once
        Set<Long> seen = new HashSet<>(trackIds.size() * 2);
        List<SqlParameterSource> updates = new ArrayList<>();
        for (int i = 0; i < trackIds.size(); i++) {
            Long trackId = trackIds.get(i);
            PlaylistTrack pt = trackId == null ? null : byTrackId.get(trackId);
            if (pt == null || !seen.add(trackId)) {
                throw new ContentException(ContentErrorCode.INVALID_TRACK_ORDER);
            }
            int newPosition = i + 1;
            if (pt.getPosition() != newPosition) {
                updates.add(new MapSqlParameterSource()
                        .addValue("position", newPosition)
                        .addValue("id", pt.getId()));
            }
        }

        if (!updates.isEmpty()) {
            jdbcTemplate.batchUpdate(Constant.SqlPath.UPDATE_POSITION_SQL, updates.toArray(SqlParameterSource[]::new));
        }
    }

    /** Non-deleted playlist or PLAYLIST_NOT_FOUND. */
    private Playlist findActive(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.PLAYLIST_NOT_FOUND));
    }

    /** Non-deleted playlist owned by the user, or PLAYLIST_NOT_FOUND / NOT_OWNER. */
    private Playlist findOwned(Long userId, Long playlistId) {
        Playlist playlist = findActive(playlistId);
        if (!playlist.getOwnerUserId().equals(userId)) {
            throw new ContentException(ContentErrorCode.NOT_OWNER);
        }
        return playlist;
    }

    /**
     * Owner always sees the playlist (no block lookup needed). Anybody else needs PUBLIC and must not be blocked.
     * Cheap checks run first so the block query is only issued when it can matter.
     */
    private void assertVisible(Playlist playlist, Long viewerId) {
        if (playlist.getOwnerUserId().equals(viewerId)) {
            return;
        }
        if (playlist.getVisibility() != PlaylistVisibility.PUBLIC
                || (viewerId != null && blockRepository.existsByBlockerIdAndBlockedId(viewerId, playlist.getOwnerUserId()))) {
            throw new ContentException(ContentErrorCode.PLAYLIST_NOT_FOUND);
        }
    }

    record PositionedTrack(int position, TrackResponse track) {}
}
