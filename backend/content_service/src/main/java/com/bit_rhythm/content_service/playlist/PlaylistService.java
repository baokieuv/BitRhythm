package com.bit_rhythm.content_service.playlist;

import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.playlist.dto.PlaylistRequest;
import com.bit_rhythm.content_service.playlist.dto.PlaylistResponse;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Playlists of the logged-in user. A playlist of somebody else is visible only when PUBLIC; every write needs ownership
 * (NOT_OWNER; for a PRIVATE playlist of somebody else answer PLAYLIST_NOT_FOUND instead, do not reveal it).
 */
@Service
public class PlaylistService {

    /** [S1] TODO(hint): ownerUserId = userId, trackCount 0. Optionally limit how many playlists one user may own. */
    public PlaylistResponse create(Long userId, PlaylistRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): PLAYLIST_NOT_FOUND when missing, or PRIVATE and the viewer is not the owner. */
    public PlaylistResponse get(Long playlistId, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): name, description, visibility. */
    public PlaylistResponse update(Long userId, Long playlistId, PlaylistRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): ONE transaction: PlaylistTrackRepository.deleteAllOfPlaylist, then the playlist row. */
    public void delete(Long userId, Long playlistId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): the playlists of the user, newest first, cursor pagination. */
    public CursorPage<PlaylistResponse> mine(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): the tracks in play order. cursor = POSITION of the last track received (null -> 0), read size + 1 rows, load the tracks in
     * ONE query (TrackService.toResponses). Tracks that are no longer PUBLISHED are left out for everybody except their artist, but they still occupy
     * their position (so the cursor stays right).
     */
    public CursorPage<TrackResponse> tracks(Long playlistId, Long viewerId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint), ONE transaction: the track must exist and be PUBLISHED (TRACK_NOT_FOUND); the playlist must not have
     * content.playlist.max-tracks tracks (PLAYLIST_FULL); TRACK_ALREADY_IN_PLAYLIST when it is already there; position = maxPosition + 1;
     * PlaylistRepository.addTracks(id, +1). Two requests at once: the unique key fails the second insert; map that to TRACK_ALREADY_IN_PLAYLIST.
     */
    public void addTrack(Long userId, Long playlistId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): deletePair; when it returned 1 correct trackCount (-1) and close the gap (positions after it - 1), or leave gaps and
     * renumber only in reorder(): positions only need to be increasing.
     */
    public void removeTrack(Long userId, Long playlistId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint), ONE transaction: trackIds must be EXACTLY the set of the tracks of the playlist, no duplicate and none missing
     * (INVALID_TRACK_ORDER); then rewrite position = 1..n in the new order.
     */
    public void reorder(Long userId, Long playlistId, List<Long> trackIds) {
        throw new UnsupportedOperationException("TODO");
    }
}
