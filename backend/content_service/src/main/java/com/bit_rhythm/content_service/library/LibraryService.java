package com.bit_rhythm.content_service.library;

import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/** The personal library of a user: liked tracks and saved albums. */
@Service
public class LibraryService {

    /**
     * [S1] TODO(hint), do NOT put @Transactional on the method: like it is done in FollowService of user-service, run the insert and the
     * counter in ONE TransactionTemplate block and catch DataIntegrityViolationException around it.
     *  1. the track must exist and be PUBLISHED (TRACK_NOT_FOUND);
     *  2. already liked -> return (idempotent);
     *  3. insert TrackLike (saveAndFlush: a duplicate fails HERE) + TrackRepository.addLikes(trackId, +1) in the same transaction;
     *  4. a duplicate from a concurrent request: ignore it (the winner counted the like).
     */
    public void likeTrack(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): TrackLikeRepository.deletePair; addLikes(trackId, -1) ONLY when it returned 1. Idempotent. */
    public void unlikeTrack(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): the liked tracks, most recently liked first (cursor = id of the TrackLike row, not of the track): size + 1 rows,
     * tracks loaded in one query, TrackService.toResponses; tracks that are no longer PUBLISHED are left out.
     */
    public CursorPage<TrackResponse> likedTracks(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): TrackLikeRepository.findByUserIdAndTrackIdIn -> set of track ids. Used by TrackService.toResponses for the "liked" flag. */
    public Set<Long> likedAmong(Long userId, Collection<Long> trackIds) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): the album must exist and be PUBLISHED (ALBUM_NOT_FOUND); idempotent; unique key + catch, like likeTrack (no counter here). */
    public void saveAlbum(Long userId, Long albumId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): SavedAlbumRepository.deletePair. Idempotent. */
    public void unsaveAlbum(Long userId, Long albumId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): the saved albums, cursor = id of the SavedAlbum row; albums no longer PUBLISHED are left out. */
    public CursorPage<AlbumResponse> savedAlbums(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): SavedAlbumRepository.findByUserIdAndAlbumIdIn -> set of album ids. Used by AlbumService.toResponses. */
    public Set<Long> savedAmong(Long userId, Collection<Long> albumIds) {
        throw new UnsupportedOperationException("TODO");
    }
}
