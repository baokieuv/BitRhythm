package com.bit_rhythm.content_service.lifecycle;

import org.springframework.stereotype.Service;

@Service
public class AccountLifecycleService {

    /**
     * [S2] TODO(hint), one @Transactional method: unknown eventType -> return (no marker); ProcessedEventRepository.existsById(eventId) ->
     * return (duplicate); otherwise insert the marker FIRST (saveAndFlush) so that a failure rolls it back with the work, then
     * USER_DISABLED -> onUserDisabled, USER_DELETED -> onUserDeleted.
     */
    public void handle(AuthEvent event) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint): the user can no longer log in: hide what they published. If the user owns an artist (ArtistRepository.findByOwnerUserId),
     * their PUBLISHED tracks and albums become HIDDEN (not TAKEN_DOWN: that status is for moderation). Playlists, likes and history are left alone.
     */
    public void onUserDisabled(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint): erase the personal data of the user:
     *  1. their playlists (PlaylistTrackRepository.deleteAllOfPlaylist for each, then the playlists);
     *  2. their likes: BEFORE deleting them, addLikes(trackId, -1) for each liked track (TrackLikeRepository.findByUserId), then deleteAllOfUser;
     *  3. their saved albums and play history (deleteAllOfUser);
     *  4. their artist, if any: the tracks and albums go (audio and cover objects deleted AFTER the commit), and so do the likes, playlist
     *     entries and saved albums that point at them. Decide with the product whether deleting an artist's music is wanted or whether it is
     *     only hidden: hiding is reversible, deleting is not.
     * The object store is not transactional: delete the objects in an afterCommit callback and never fail the event over a missing object.
     */
    public void onUserDeleted(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }
}
