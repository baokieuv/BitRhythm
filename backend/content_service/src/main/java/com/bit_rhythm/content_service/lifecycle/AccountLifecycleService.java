package com.bit_rhythm.content_service.lifecycle;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.artist.ArtistRepository;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.history.PlayHistoryRepository;
import com.bit_rhythm.content_service.library.SavedAlbumRepository;
import com.bit_rhythm.content_service.library.TrackLike;
import com.bit_rhythm.content_service.library.TrackLikeRepository;
import com.bit_rhythm.content_service.playlist.Playlist;
import com.bit_rhythm.content_service.playlist.PlaylistRepository;
import com.bit_rhythm.content_service.playlist.PlaylistTrackRepository;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reacts to the account events of auth-service. Every event is handled in ONE transaction whose first write is the
 * processed_events marker: a failure rolls the marker back together with the work, so the retry starts again from scratch,
 * and an event delivered twice is skipped the second time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLifecycleService {

    static final String USER_DISABLED = "USER_DISABLED";
    static final String USER_DELETED = "USER_DELETED";

    private final ProcessedEventRepository processedEventRepository;
    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final TrackLikeRepository trackLikeRepository;
    private final SavedAlbumRepository savedAlbumRepository;
    private final PlayHistoryRepository playHistoryRepository;

    /** Idempotent entry point. Unknown event types leave no marker; a duplicate eventId is skipped. */
    @Transactional
    public void handle(AuthEvent event) {

        String type = event.eventType();
        if (!USER_DISABLED.equals(type) && !USER_DELETED.equals(type)) {
            return;
        }

        if (processedEventRepository.existsById(event.eventId())) {
            log.debug("event {} already handled, skipped", event.eventId());
            return;
        }

        // the marker goes first: if two consumers race, the loser fails HERE (duplicate key), rolls back and its retry
        // finds the marker and skips
        processedEventRepository.saveAndFlush(new ProcessedEvent(event.eventId()));

        if (USER_DISABLED.equals(type)) {
            onUserDisabled(event.userId());
        } else {
            onUserDeleted(event.userId());
        }
        log.info("handled {} ({}) for user {}", type, event.eventId(), event.userId());
    }

    /**
     * The user can no longer log in: hide what they published. PUBLISHED tracks and albums of their artist become HIDDEN (not
     * TAKEN_DOWN: that status is for moderation). Drafts, playlists, likes and history are left alone.
     */
    @Transactional
    public void onUserDisabled(Long userId) {
        artistRepository.findByOwnerUserId(userId).ifPresent(artist -> hideMusicOf(artist.getId()));
    }

    /**
     * Erase the personal data of the user.
     *  1. their playlists and the entries of those playlists;
     *  2. their likes, after taking each liked track's like_count down by one;
     *  3. their saved albums and play history;
     *  4. their artist's music is HIDDEN, not deleted (PRODUCT DECISION, see below).
     * <p>
     * Deleting an artist's music is irreversible and also has to remove the likes, playlist entries and saved albums that point at
     * it, plus the audio and cover objects (in an afterCommit callback, never failing the event over a missing object). Until the
     * product decides, the safe choice is the reversible one: hide it.
     */
    @Transactional
    public void onUserDeleted(Long userId) {

        // 1. playlists: entries first, then the playlists
        List<Playlist> playlists = playlistRepository.findByOwnerUserId(userId);
        for (Playlist playlist : playlists) {
            playlistTrackRepository.deleteAllOfPlaylist(playlist.getId());
        }
        playlistRepository.deleteAll(playlists);
        playlistRepository.flush();

        // 2. likes: correct the counters BEFORE the rows disappear
        List<TrackLike> likes = trackLikeRepository.findByUserId(userId);
        for (TrackLike like : likes) {
            trackRepository.addLikes(like.getTrackId(), -1);
        }
        trackLikeRepository.deleteAllOfUser(userId);

        // 3. saved albums and history
        savedAlbumRepository.deleteAllOfUser(userId);
        playHistoryRepository.deleteAllOfUser(userId);

        // 4. the artist, if any
        artistRepository.findByOwnerUserId(userId).ifPresent(artist -> hideMusicOf(artist.getId()));

        log.info("erased personal data of user {}: {} playlists, {} likes", userId, playlists.size(), likes.size());
    }

    // ---------------------------------------------------------------------------------------------------------------

    /** PUBLISHED -> HIDDEN for every track and album of the artist (artistId = artists.id). Managed entities: dirty checking saves. */
    private void hideMusicOf(Long artistId) {

        int tracks = 0;
        for (Track track : trackRepository.findByArtistId(artistId)) {
            if (track.getStatus() == ContentStatus.PUBLISHED) {
                track.setStatus(ContentStatus.HIDDEN);
                tracks++;
            }
        }

        int albums = 0;
        for (Album album : albumRepository.findByArtistId(artistId)) {
            if (album.getStatus() == ContentStatus.PUBLISHED) {
                album.setStatus(ContentStatus.HIDDEN);
                albums++;
            }
        }

        log.info("hid {} tracks and {} albums of artist {}", tracks, albums, artistId);
    }
}