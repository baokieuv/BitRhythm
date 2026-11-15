package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.track.dto.TrackRequest;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

/**
 * The artist's own tracks. EVERY method starts with ArtistService.requireOwnedBy(userId) and only touches tracks whose
 * artistId equals that artist's id: a track of somebody else answers TRACK_NOT_FOUND (do not reveal it exists).
 */
@Service
public class StudioTrackService {

    /**
     * [S1] TODO(hint): a new track starts as DRAFT with audioStatus PENDING_UPLOAD. If albumId is given the album must belong to the same
     * artist (ALBUM_NOT_FOUND otherwise); genreId must exist (GENRE_NOT_FOUND); coverKey must start with "covers/{userId}/" and exist in
     * the cover bucket (COVER_NOT_UPLOADED).
     */
    public TrackResponse create(Long userId, TrackRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): all statuses, newest first, cursor pagination (TrackRepository.findByArtistIdAndIdLessThanOrderByIdDesc). */
    public CursorPage<TrackResponse> list(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): includes audioStatus and status so the studio UI can show what is missing. */
    public TrackResponse get(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): same validations as create; a new coverKey replaces the old one (delete the old object AFTER the update). Never touches audio, status or counters. */
    public TrackResponse update(Long userId, Long trackId, TrackRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): delete the row, then (after commit) the audio object and the cover; also remove the track from playlists, likes and
     * history (PlaylistTrackRepository.deleteAllOfTrack, TrackLikeRepository...) and correct playlist.trackCount. Consider HIDDEN
     * instead of deleting a PUBLISHED track that listeners already have in playlists.
     */
    public void delete(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): needs audioStatus READY (AUDIO_NOT_READY); sets status PUBLISHED and publishedAt once (keep the first date when
     * published again). Idempotent. [S2] writes a TRACK_PUBLISHED event to the outbox in the same transaction (ContentEventPublisher),
     * only on the first publication.
     */
    public TrackResponse publish(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): PUBLISHED -> HIDDEN (the owner's "unpublish"); DRAFT stays DRAFT. publish() on a TAKEN_DOWN track answers TAKEN_DOWN: only an admin can restore it (AdminCatalogService). */
    public TrackResponse unpublish(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }
}
