package com.bit_rhythm.content_service.track;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.artist.ArtistRepository;
import com.bit_rhythm.content_service.block.BlockRepository;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.genre.Genre;
import com.bit_rhythm.content_service.genre.GenreRepository;
import com.bit_rhythm.content_service.library.TrackLikeRepository;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The listener's view of the catalog: only PUBLISHED tracks (an owner also sees their own drafts). */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private static final int NOT_DELETED = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

    private final TrackRepository trackRepository;
    private final BlockRepository blockRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final TrackLikeRepository trackLikeRepository;
    private final StorageClient storageClient;
    private final ContentProperties properties;

    /** Get a track by ID, if it's visible to the viewer. */
    public TrackResponse get(Long trackId, Long viewerId) {

        Track track = trackRepository.findByIdAndIsDeleted(trackId, NOT_DELETED).orElseThrow(
                () -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        // Track.artistId is artists.id, NOT a user id: ownership and blocks are decided on the artist's owner user.
        Long ownerUserId = artistRepository.findById(track.getArtistId()).map(Artist::getOwnerUserId).orElse(null);

        boolean isOwner = ownerUserId != null && ownerUserId.equals(viewerId);

        if (track.getStatus() != ContentStatus.PUBLISHED && !isOwner) {
            throw new ContentException(ContentErrorCode.TRACK_NOT_FOUND);
        }

        if (ownerUserId != null && blockRepository.existsByBlockerIdAndBlockedId(viewerId, ownerUserId)) {
            throw new ContentException(ContentErrorCode.TRACK_NOT_FOUND);
        }

        return toResponses(List.of(track), viewerId).getFirst();
    }

    /** New releases, optionally of one genre (genreId null = all). Keyset pagination by id, newest first. */
    public CursorPage<TrackResponse> listNew(Long genreId, Long cursor, int size, Long viewerId) {

        int limit = clampSize(size);
        Pageable pageable = PageRequest.of(0, limit + 1);
        long start = Paging.startCursor(cursor);

        List<Track> rows = genreId == null
                ? trackRepository.findByStatusAndIdLessThanOrderByIdDesc(ContentStatus.PUBLISHED, start, pageable)
                : trackRepository.findByStatusAndGenreIdAndIdLessThanOrderByIdDesc(ContentStatus.PUBLISHED, genreId, start, pageable);

        return toPage(rows, limit, viewerId);
    }

    /** PUBLISHED tracks of one artist (the artist page), same pagination. artistId is artists.id. */
    public CursorPage<TrackResponse> listByArtist(Long artistId, Long cursor, int size, Long viewerId) {

        Artist artist = artistRepository.findById(artistId).orElseThrow(
                () -> new ContentException(ContentErrorCode.ARTIST_NOT_FOUND));

        if (blockRepository.existsByBlockerIdAndBlockedId(viewerId, artist.getOwnerUserId())) {
            throw new ContentException(ContentErrorCode.ARTIST_NOT_FOUND);
        }

        int limit = clampSize(size);
        List<Track> rows = trackRepository.findByArtistIdAndStatusAndIdLessThanOrderByIdDesc(
                artistId, ContentStatus.PUBLISHED, Paging.startCursor(cursor), PageRequest.of(0, limit + 1));

        return toPage(rows, limit, viewerId);
    }

    /** The PUBLISHED tracks of an album in track-number order. The album must be visible to the viewer (same rule as AlbumService.get). */
    public List<TrackResponse> listByAlbum(Long albumId, Long viewerId) {

        Album album = albumRepository.findByIdAndIsDeleted(albumId, NOT_DELETED).orElseThrow(
                () -> new ContentException(ContentErrorCode.ALBUM_NOT_FOUND));

        Long ownerUserId = artistRepository.findById(album.getArtistId()).map(Artist::getOwnerUserId).orElse(null);
        boolean isOwner = ownerUserId != null && ownerUserId.equals(viewerId);

        if (album.getStatus() != ContentStatus.PUBLISHED && !isOwner) {
            throw new ContentException(ContentErrorCode.ALBUM_NOT_FOUND);
        }

        if (ownerUserId != null && blockRepository.existsByBlockerIdAndBlockedId(viewerId, ownerUserId)) {
            throw new ContentException(ContentErrorCode.ALBUM_NOT_FOUND);
        }

        return toResponses(
                trackRepository.findByAlbumIdAndStatusOrderByTrackNumberAscIdAsc(albumId, ContentStatus.PUBLISHED),
                viewerId);
    }

    /**
     * The track entity if the viewer may see it (PUBLISHED, or the viewer owns it; and the viewer has not blocked its artist),
     * else TRACK_NOT_FOUND. The single visibility rule for everything that hangs off a track (lyrics, ...).
     */
    public Track findVisible(Long trackId, Long viewerId) {

        Track track = trackRepository.findByIdAndIsDeleted(trackId, NOT_DELETED).orElseThrow(
                () -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));

        // Track.artistId is artists.id, NOT a user id: ownership and blocks are decided on the artist's owner user.
        Long ownerUserId = artistRepository.findById(track.getArtistId()).map(Artist::getOwnerUserId).orElse(null);

        boolean isOwner = ownerUserId != null && ownerUserId.equals(viewerId);

        if (track.getStatus() != ContentStatus.PUBLISHED && !isOwner) {
            throw new ContentException(ContentErrorCode.TRACK_NOT_FOUND);
        }

        if (ownerUserId != null && blockRepository.existsByBlockerIdAndBlockedId(viewerId, ownerUserId)) {
            throw new ContentException(ContentErrorCode.TRACK_NOT_FOUND);
        }

        return track;
    }

    /**
     * The single place that builds a TrackResponse for every list of tracks (search, playlists, library, discovery).
     * Exactly one query each for artists, albums, genres and the viewer's likes for the whole page, never one per track.
     * Order of the input is preserved.
     */
    public List<TrackResponse> toResponses(List<Track> tracks, Long viewerId) {

        if (tracks == null || tracks.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> trackIds = ids(tracks, Track::getId);
        Set<Long> artistIds = ids(tracks, Track::getArtistId);
        Set<Long> albumIds = ids(tracks, Track::getAlbumId);
        Set<Long> genreIds = ids(tracks, Track::getGenreId);

        Map<Long, Artist> artists = artistRepository.findByIdIn(artistIds).stream()
                .collect(Collectors.toMap(Artist::getId, Function.identity(), (a, b) -> a));

        Map<Long, Album> albums = albumIds.isEmpty() ? Map.of() : albumRepository.findByIdIn(albumIds).stream()
                .collect(Collectors.toMap(Album::getId, Function.identity(), (a, b) -> a));

        Map<Long, Genre> genres = genreIds.isEmpty() ? Map.of() : genreRepository.findByIdIn(genreIds).stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity(), (a, b) -> a));

        Set<Long> likedIds = trackLikeRepository.findLikedTrackIds(viewerId, trackIds);

        String coverBucket = properties.cover().bucket();

        return tracks.stream().map(track -> {
            Artist artist = artists.getOrDefault(track.getArtistId(), new Artist());
            Album album = track.getAlbumId() == null ? new Album() : albums.getOrDefault(track.getAlbumId(), new Album());
            Genre genre = track.getGenreId() == null ? new Genre() : genres.getOrDefault(track.getGenreId(), new Genre());

            // Track cover, else the album cover.
            String coverKey = track.getCoverKey() != null ? track.getCoverKey() : album.getCoverKey();
            String coverUrl = coverKey == null ? null : storageClient.publicUrl(coverBucket, coverKey);

            return TrackResponse.from(track, coverUrl, artist, album, genre, likedIds.contains(track.getId()));
        }).toList();
    }

    // ---------------------------------------------------------------------------------------------------------------

    private int clampSize(int size) {
        return Paging.clampSize(size, properties.pagination().maxPageSize());
    }

    /** rows were fetched with limit + 1: the extra row only tells us there is a next page and is never returned. */
    private CursorPage<TrackResponse> toPage(List<Track> rows, int limit, Long viewerId) {
        boolean hasMore = rows.size() > limit;
        List<Track> pageRows = hasMore ? rows.subList(0, limit) : rows;
        Long nextCursor = hasMore ? pageRows.getLast().getId() : null;
        return new CursorPage<>(toResponses(pageRows, viewerId), nextCursor);
    }

    private static <T> Set<Long> ids(List<T> items, Function<T, Long> getter) {
        return items.stream().map(getter).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}