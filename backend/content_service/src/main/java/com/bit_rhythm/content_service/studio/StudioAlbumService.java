package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.album.AlbumService;
import com.bit_rhythm.content_service.album.dto.AlbumRequest;
import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.library.SavedAlbumRepository;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

/** The artist's own albums. Same ownership rule as StudioTrackService: the artist of userId first; an album of somebody else is ALBUM_NOT_FOUND. */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudioAlbumService {

    private final StudioOwnership ownership;
    private final CoverKeyChecker coverKeyChecker;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final SavedAlbumRepository savedAlbumRepository;
    private final AlbumService albumService;
    private final StorageClient storageClient;
    private final TransactionTemplate transactionTemplate;
    private final ContentProperties properties;

    /** A DRAFT album; the cover key is validated like in StudioTrackService (folder "covers/{userId}/" + stat). */
    public AlbumResponse create(Long userId, AlbumRequest request) {

        Artist artist = ownership.artist(userId);
        coverKeyChecker.check(userId, request.coverKey());

        Album album = new Album();
        album.setArtistId(artist.getId());
        album.setTitle(request.title());
        album.setReleaseDate(request.releaseDate());
        album.setCoverKey(request.coverKey());
        albumRepository.save(album);

        return view(album, userId);
    }

    /** All statuses, newest first, keyset pagination by id. */
    public CursorPage<AlbumResponse> list(Long userId, Long cursor, int size) {

        Artist artist = ownership.artist(userId);
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<Album> fetched = albumRepository.findByArtistIdAndIdLessThanOrderByIdDesc(
                artist.getId(), Paging.startCursor(cursor), PageRequest.ofSize(limit + 1));

        boolean hasMore = fetched.size() > limit;
        List<Album> albums = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? albums.getLast().getId() : null;

        return new CursorPage<>(albumService.toResponses(albums, userId), nextCursor);
    }

    /** Title, releaseDate and coverKey only (a new cover replaces the old object AFTER the update; null removes the cover). */
    public AlbumResponse update(Long userId, Long albumId, AlbumRequest request) {

        Artist artist = ownership.artist(userId);
        Album album = ownership.album(artist, albumId);

        String oldCoverKey = album.getCoverKey();
        if (request.coverKey() != null && !request.coverKey().equals(oldCoverKey)) {
            coverKeyChecker.check(userId, request.coverKey());
        }

        album.setTitle(request.title());
        album.setReleaseDate(request.releaseDate());
        album.setCoverKey(request.coverKey());
        albumRepository.save(album);

        if (oldCoverKey != null && !oldCoverKey.equals(album.getCoverKey())) {
            deleteQuietly(oldCoverKey);
        }

        return view(album, userId);
    }

    /**
     * The tracks of the album are NOT deleted: they are detached (albumId = null, trackNumber = null). The saved-album entries of
     * listeners go with the album. All in ONE transaction; the cover object is deleted after the commit.
     */
    public void delete(Long userId, Long albumId) {

        Artist artist = ownership.artist(userId);
        Album album = ownership.album(artist, albumId);
        String coverKey = album.getCoverKey();

        transactionTemplate.executeWithoutResult(status -> {

            List<Track> tracks = trackRepository.findByAlbumIdOrderByTrackNumberAscIdAsc(albumId);

            for(Track track : tracks) {
                track.setAlbumId(null);
                track.setTrackNumber(null);
            }

            trackRepository.saveAll(tracks);
            savedAlbumRepository.deleteAllOfAlbum(albumId);
            albumRepository.deleteById(albumId);
        });

        deleteQuietly(coverKey);
    }

    /**
     * DRAFT or HIDDEN -> PUBLISHED (TAKEN_DOWN answers TAKEN_DOWN), publishedAt only the first time, idempotent. Publishing the album
     * does NOT publish its tracks: the artist publishes each track.
     */
    public AlbumResponse publish(Long userId, Long albumId) {

        Artist artist = ownership.artist(userId);
        Album album = ownership.album(artist, albumId);

        if (album.getStatus() == ContentStatus.TAKEN_DOWN) {
            throw new ContentException(ContentErrorCode.TAKEN_DOWN);
        }
        if (album.getStatus() != ContentStatus.PUBLISHED) {
            album.setStatus(ContentStatus.PUBLISHED);
            if (album.getPublishedAt() == null) {
                album.setPublishedAt(Instant.now());
            }
            albumRepository.save(album);
        }

        return view(album, userId);
    }

    /** PUBLISHED -> HIDDEN; every other status stays as it is. */
    public AlbumResponse unpublish(Long userId, Long albumId) {
        Artist artist = ownership.artist(userId);
        Album album = ownership.album(artist, albumId);

        if (album.getStatus() == ContentStatus.PUBLISHED) {
            album.setStatus(ContentStatus.HIDDEN);
            albumRepository.save(album);
        }

        return view(album, userId);
    }


    private AlbumResponse view(Album album, Long userId) {
        return albumService.toResponses(List.of(album), userId).getFirst();
    }

    /** The object store is not transactional: a failed delete never fails the request. */
    private void deleteQuietly(String key) {
        if (key == null) {
            return;
        }
        try {
            storageClient.delete(properties.cover().bucket(), key);
        } catch (Exception e) {
            log.warn("could not delete cover {}: {}", key, e.toString());
        }
    }
}
