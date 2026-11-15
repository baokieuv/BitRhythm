package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.album.dto.AlbumRequest;
import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.common.CursorPage;
import org.springframework.stereotype.Service;

/** The artist's own albums. Same ownership rule as StudioTrackService: requireOwnedBy(userId) first; an album of somebody else is ALBUM_NOT_FOUND. */
@Service
public class StudioAlbumService {

    /** [S1] TODO(hint): DRAFT album; coverKey validated like in StudioTrackService (prefix "covers/{userId}/" + stat). */
    public AlbumResponse create(Long userId, AlbumRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): all statuses, newest first, cursor pagination. */
    public CursorPage<AlbumResponse> list(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): title, releaseDate, coverKey only. */
    public AlbumResponse update(Long userId, Long albumId, AlbumRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): the tracks of the album are NOT deleted: detach them (albumId = null, trackNumber = null). */
    public void delete(Long userId, Long albumId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): DRAFT/HIDDEN -> PUBLISHED (TAKEN_DOWN answers TAKEN_DOWN), publishedAt once. Publishing the album does not publish its tracks (the artist publishes each track). */
    public AlbumResponse publish(Long userId, Long albumId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): PUBLISHED -> HIDDEN. */
    public AlbumResponse unpublish(Long userId, Long albumId) {
        throw new UnsupportedOperationException("TODO");
    }
}
