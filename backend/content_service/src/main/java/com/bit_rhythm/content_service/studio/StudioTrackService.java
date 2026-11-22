package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.event.ContentEventPublisher;
import com.bit_rhythm.content_service.genre.GenreRepository;
import com.bit_rhythm.content_service.history.PlayHistoryRepository;
import com.bit_rhythm.content_service.library.TrackLikeRepository;
import com.bit_rhythm.content_service.lyrics.CreditRepository;
import com.bit_rhythm.content_service.lyrics.LyricsRepository;
import com.bit_rhythm.content_service.playlist.PlaylistRepository;
import com.bit_rhythm.content_service.playlist.PlaylistTrack;
import com.bit_rhythm.content_service.playlist.PlaylistTrackRepository;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.track.AudioStatus;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.content_service.track.TrackService;
import com.bit_rhythm.content_service.track.dto.TrackRequest;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

/**
 * The artist's own tracks. EVERY method starts with the ownership check (StudioOwnership.artist) and only touches tracks whose
 * artistId equals that artist's id: a track of somebody else answers TRACK_NOT_FOUND (its existence is not revealed).
 * Writes that touch several tables use a TransactionTemplate, so object-store deletes can run strictly AFTER the commit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudioTrackService {

    private final StudioOwnership ownership;
    private final CoverKeyChecker coverKeyChecker;
    private final TrackRepository trackRepository;
    private final GenreRepository genreRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final TrackLikeRepository trackLikeRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final LyricsRepository lyricsRepository;
    private final CreditRepository creditRepository;
    private final TrackService trackService;
    private final ContentEventPublisher eventPublisher;
    private final StorageClient storageClient;
    private final TransactionTemplate transactionTemplate;
    private final ContentProperties properties;

    /** A new track starts as DRAFT with audioStatus PENDING_UPLOAD. */
    public TrackResponse create(Long userId, TrackRequest request) {

        Artist artist = ownership.artist(userId);
        validateReferences(userId, artist, request, null);

        Track track = new Track(artist.getId(), request.title());
        apply(track, request);
        trackRepository.save(track);

        return view(track, userId);
    }

    /** All statuses, newest first, keyset pagination by id. */
    public CursorPage<TrackResponse> list(Long userId, Long cursor, int size) {
        Artist artist = ownership.artist(userId);
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<Track> fetched = trackRepository.findByArtistIdAndIdLessThanOrderByIdDesc(
                artist.getId(), Paging.startCursor(cursor), PageRequest.ofSize(limit + 1));

        boolean hasMore = fetched.size() > limit;
        List<Track> tracks = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? tracks.getLast().getId() : null;

        return new CursorPage<>(trackService.toResponses(tracks, userId), nextCursor);
    }

    /** Includes audioStatus and status, so the studio UI can show what is missing. */
    public TrackResponse get(Long userId, Long trackId) {
        Artist artist = ownership.artist(userId);
        return view(ownership.track(artist, trackId), userId);
    }

    /**
     * Same validations as create. Never touches audio, status or counters. coverKey: a new key replaces the old object (deleted AFTER
     * the update); the same key again changes nothing; null removes the cover (the track then shows its album cover).
     */
    public TrackResponse update(Long userId, Long trackId, TrackRequest request) {
        Artist artist = ownership.artist(userId);
        Track track = ownership.track(artist, trackId);

        String oldCoverKey = track.getCoverKey();
        validateReferences(userId, artist, request, oldCoverKey);

        track.setTitle(request.title());
        apply(track, request);
        trackRepository.save(track);

        if (oldCoverKey != null && !oldCoverKey.equals(track.getCoverKey())) {
            deleteQuietly(properties.cover().bucket(), oldCoverKey);
        }
        return view(track, userId);
    }

    /**
     * Delete the row and everything that points at it (playlist entries, with each playlist's trackCount corrected, likes, history,
     * lyrics, credits) in ONE transaction; the audio and cover objects are deleted AFTER the commit and never fail the request.
     * PRODUCT NOTE: this also removes a PUBLISHED track from the playlists of listeners. If that is not wanted, unpublish() hides it
     * instead and keeps it everywhere.
     */
    public void delete(Long userId, Long trackId) {
        Artist artist = ownership.artist(userId);
        Track track = ownership.track(artist, trackId);

        String audioKey = track.getAudioKey();
        String coverKey = track.getCoverKey();

        transactionTemplate.executeWithoutResult(status -> {
            for (PlaylistTrack entry : playlistTrackRepository.findByTrackId(trackId)) {
                playlistRepository.addTracks(entry.getPlaylistId(), -1);   // correct the playlist's trackCount
            }
            playlistTrackRepository.deleteAllOfTrack(trackId);
            trackLikeRepository.deleteAllOfTrack(trackId);
            playHistoryRepository.deleteAllOfTrack(trackId);
            creditRepository.deleteAllOfTrack(trackId);
            lyricsRepository.deleteByTrackId(trackId);
            trackRepository.deleteById(trackId);
        });

        deleteQuietly(properties.audio().bucket(), audioKey);
        deleteQuietly(properties.cover().bucket(), coverKey);
    }

    /**
     * Needs audioStatus READY (AUDIO_NOT_READY). Sets status PUBLISHED and publishedAt ONCE (the first date is kept when published
     * again). Idempotent. A TAKEN_DOWN track answers TAKEN_DOWN: only an admin can restore it. On the FIRST publication a
     * TRACK_PUBLISHED event is written to the outbox in the same transaction (two simultaneous first publications can both write it:
     * consumers deduplicate by the deterministic eventId).
     */
    public TrackResponse publish(Long userId, Long trackId) {

        Artist artist = ownership.artist(userId);
        ownership.track(artist, trackId);   // ownership check; the row is read again inside the transaction

        Track published = transactionTemplate.execute(status -> {
            Track track = trackRepository.findById(trackId)
                    .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

            if (track.getStatus() == ContentStatus.TAKEN_DOWN) {
                throw new ContentException(ContentErrorCode.TAKEN_DOWN);
            }
            if (track.getStatus() == ContentStatus.PUBLISHED) {
                return track;   // idempotent
            }
            if (track.getAudioStatus() != AudioStatus.READY) {
                throw new ContentException(ContentErrorCode.AUDIO_NOT_READY);
            }

            boolean firstPublication = track.getPublishedAt() == null;
            track.setStatus(ContentStatus.PUBLISHED);
            if (firstPublication) {
                track.setPublishedAt(Instant.now());
                eventPublisher.publishTrackPublished(track, artist);   // the outbox write is in the same transaction
            }
            return trackRepository.save(track);
        });

        return view(published, userId);
    }

    /** PUBLISHED -> HIDDEN (the owner's "unpublish"); every other status stays as it is. */
    public TrackResponse unpublish(Long userId, Long trackId) {
        Artist artist = ownership.artist(userId);
        Track track = ownership.track(artist, trackId);

        if (track.getStatus() == ContentStatus.PUBLISHED) {
            track.setStatus(ContentStatus.HIDDEN);
            trackRepository.save(track);
        }
        return view(track, userId);
    }

    /** album of the same artist (ALBUM_NOT_FOUND), genre exists (GENRE_NOT_FOUND), cover key valid (COVER_*). */
    private void validateReferences(Long userId, Artist artist, TrackRequest request, String currentCoverKey) {

        if (request.albumId() != null) {
            ownership.album(artist, request.albumId());
        }
        if (request.genreId() != null && !genreRepository.existsById(request.genreId())) {
            throw new ContentException(ContentErrorCode.GENRE_NOT_FOUND);
        }
        // the same key as today was checked when it was first accepted: do not stat it again
        if (request.coverKey() != null && !request.coverKey().equals(currentCoverKey)) {
            coverKeyChecker.check(userId, request.coverKey());
        }
    }

    /** The fields an artist may edit. Audio, status and counters are never touched here. */
    private void apply(Track track, TrackRequest request) {
        track.setAlbumId(request.albumId());
        track.setTrackNumber(request.albumId() == null ? null : request.trackNumber());   // a number only makes sense in an album
        track.setGenreId(request.genreId());
        track.setCoverKey(request.coverKey());
        track.setExplicitContent(request.explicitContent());
    }

    private TrackResponse view(Track track, Long userId) {
        return trackService.toResponses(List.of(track), userId).getFirst();
    }

    /** The object store is not transactional: a failed delete never fails the request. */
    private void deleteQuietly(String bucket, String key) {
        if (key == null) {
            return;
        }
        try {
            storageClient.delete(bucket, key);
        } catch (Exception e) {
            log.warn("could not delete object {}/{}: {}", bucket, key, e.toString());
        }
    }
}
