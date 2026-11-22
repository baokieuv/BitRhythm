package com.bit_rhythm.content_service.album;

import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.artist.ArtistRepository;
import com.bit_rhythm.content_service.block.BlockRepository;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.library.SavedAlbum;
import com.bit_rhythm.content_service.library.SavedAlbumRepository;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The listener's view of albums: only PUBLISHED ones (an owner also sees their own drafts).

 * Album.artistId is artists.id, NOT a user id: ownership and blocks are decided on the artist's owner user.
 * This class reads saved albums straight from SavedAlbumRepository and does not use LibraryService, because LibraryService
 * depends on AlbumService (a circular dependency otherwise).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private static final int NOT_DELETED = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final BlockRepository blockRepository;
    private final SavedAlbumRepository savedAlbumRepository;
    private final StorageClient storageClient;
    private final ContentProperties properties;

    /** Get an album by its ID, if it's published or if the viewer is the owner. */
    public AlbumResponse get(Long albumId, Long viewerId) {

        Album album = albumRepository.findByIdAndIsDeleted(albumId, NOT_DELETED)
                .orElseThrow(() -> new ContentException(ContentErrorCode.ALBUM_NOT_FOUND, "id " + albumId));

        Artist artist = artistRepository.findById(album.getArtistId())
                .orElseThrow(() -> new ContentException(ContentErrorCode.ARTIST_NOT_FOUND, "id " + album.getArtistId()));

        boolean isOwner = artist.getOwnerUserId().equals(viewerId);

        if (album.getStatus() != ContentStatus.PUBLISHED && !isOwner) {
            throw new ContentException(ContentErrorCode.ALBUM_NOT_FOUND, "id " + albumId);
        }

        if (!isOwner && viewerId != null && blockRepository.existsByBlockerIdAndBlockedId(viewerId, artist.getOwnerUserId())) {
            throw new ContentException(ContentErrorCode.ALBUM_NOT_FOUND, "id " + albumId);
        }

        boolean saved = viewerId != null && savedAlbumRepository.existsByUserIdAndAlbumId(viewerId, albumId);
        return new AlbumResponse(album, artist, coverUrl(album), saved);
    }

    /** Get a page of published albums, newest first. */
    public CursorPage<AlbumResponse> listPublished(Long cursor, int size, Long viewerId) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<Album> fetched = albumRepository.findByStatusAndIdLessThanExcludingBlockedViewer(
                ContentStatus.PUBLISHED, Paging.startCursor(cursor), viewerId, PageRequest.of(0, limit + 1));

        return toPage(fetched, limit, viewerId);
    }

    /** Get a page of PUBLISHED albums of one artist (artistId = artists.id), newest first. */
    public CursorPage<AlbumResponse> listByArtist(Long artistId, Long cursor, int size, Long viewerId) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.ARTIST_NOT_FOUND, "id " + artistId));

        if (viewerId != null && blockRepository.existsByBlockerIdAndBlockedId(viewerId, artist.getOwnerUserId())) {
            throw new ContentException(ContentErrorCode.ARTIST_NOT_FOUND, "id " + artistId);
        }

        List<Album> fetched = albumRepository.findByArtistIdAndStatusAndIdLessThanOrderByIdDesc(
                artistId, ContentStatus.PUBLISHED, Paging.startCursor(cursor), PageRequest.of(0, limit + 1));

        return toPage(fetched, limit, viewerId);
    }

    /**
     * The single place that builds an AlbumResponse for every list of albums (browse, artist page, library).
     * One query for the artists and one for the viewer's saved flags of the whole page, never one per album. Order is preserved.
     * An album whose artist no longer exists is left out.
     */
    public List<AlbumResponse> toResponses(List<Album> albums, Long viewerId) {

        if (albums == null || albums.isEmpty()) {
            return List.of();
        }

        Set<Long> artistIds = albums.stream().map(Album::getArtistId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> albumIds = albums.stream().map(Album::getId).collect(Collectors.toSet());

        Map<Long, Artist> artists = artistRepository.findByIdIn(artistIds).stream()
                .collect(Collectors.toMap(Artist::getId, Function.identity(), (a, b) -> a));

        Set<Long> savedIds = viewerId == null ? Set.of()
                : savedAlbumRepository.findByUserIdAndAlbumIdIn(viewerId, albumIds).stream()
                .map(SavedAlbum::getAlbumId)
                .collect(Collectors.toSet());

        return albums.stream()
                .filter(album -> {
                    boolean known = artists.containsKey(album.getArtistId());
                    if (!known) {
                        log.warn("Leaving out album {}: artist {} not found", album.getId(), album.getArtistId());
                    }
                    return known;
                })
                .map(album -> new AlbumResponse(album, artists.get(album.getArtistId()), coverUrl(album),
                        savedIds.contains(album.getId())))
                .toList();
    }

    // ---------------------------------------------------------------------------------------------------------------

    /** rows were fetched with limit + 1: the extra row only says there is a next page. The cursor is the last row EXAMINED. */
    private CursorPage<AlbumResponse> toPage(List<Album> fetched, int limit, Long viewerId) {
        boolean hasMore = fetched.size() > limit;
        List<Album> albums = hasMore ? fetched.subList(0, limit) : fetched;
        Long nextCursor = hasMore ? albums.getLast().getId() : null;
        return new CursorPage<>(toResponses(albums, viewerId), nextCursor);
    }

    private String coverUrl(Album album) {
        return album.getCoverKey() == null ? null : storageClient.publicUrl(properties.cover().bucket(), album.getCoverKey());
    }
}