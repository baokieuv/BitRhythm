package com.bit_rhythm.content_service.admin;

import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

/** Moderation of the catalog (ROLE_ADMIN). A taken-down item is TAKEN_DOWN, a status the owner cannot change. */
@Service
public class AdminCatalogService {

    /** [S1] TODO(hint): status == null -> every status; newest first, cursor pagination; mapped with TrackService.toResponses (viewer = the admin). */
    public CursorPage<TrackResponse> tracks(ContentStatus status, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): status TAKEN_DOWN (TRACK_NOT_FOUND if missing); idempotent. The track disappears from lists, search and streaming; playlists keep their rows but skip it. */
    public void takeDownTrack(Long trackId, Long adminId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): TAKEN_DOWN -> HIDDEN (not straight to PUBLISHED: the owner decides to publish again). */
    public void restoreTrack(Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): like tracks(). */
    public CursorPage<AlbumResponse> albums(ContentStatus status, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): status TAKEN_DOWN; the tracks of the album are not touched (take them down one by one if needed). */
    public void takeDownAlbum(Long albumId, Long adminId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): TAKEN_DOWN -> HIDDEN. */
    public void restoreAlbum(Long albumId) {
        throw new UnsupportedOperationException("TODO");
    }
}
