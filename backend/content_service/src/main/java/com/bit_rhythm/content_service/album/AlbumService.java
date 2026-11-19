package com.bit_rhythm.content_service.album;

import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.artist.ArtistRepository;
import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.common.Paging;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final StorageClient storageClient;
    private final ContentProperties properties;

    /** Get an album by its ID, if it's published or if the viewer is the owner. */
    public AlbumResponse get(Long albumId, Long viewerId) {

        Album album = albumRepository.findByIdAndIsDeleted(albumId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ContentException(ContentErrorCode.ALBUM_NOT_FOUND, "id " + albumId));

        Artist artist = artistRepository.findByOwnerUserIdAndIsDeleted(album.getArtistId(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ContentException(ContentErrorCode.ARTIST_NOT_FOUND, "id " + album.getArtistId()));

        String coverUrl = storageClient.publicUrl(properties.cover().bucket(), album.getCoverKey());

        if (album.getStatus() == ContentStatus.PUBLISHED || album.getArtistId().equals(viewerId)) {
            return new AlbumResponse(album, artist, coverUrl);
        } else {
            throw new ContentException(ContentErrorCode.ALBUM_NOT_FOUND, "id " + albumId);
        }
    }

    /** Get a page of published albums, newest first. */
    public CursorPage<AlbumResponse> listPublished(Long cursor, int size, Long viewerId) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());
        long lastExamined = cursor == null ? 0L : cursor;

        List<Album> albums = albumRepository.findByStatusAndIdLessThanOrderByIdDesc(
                ContentStatus.PUBLISHED, Paging.startCursor(cursor), PageRequest.of(0, limit + 1));

        Map<Long, Artist> artists = artistRepository.findByIdIn(
                albums.stream().map(Album::getArtistId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Artist::getOwnerUserId, Function.identity()));


        List<AlbumResponse> responses = albums.stream().map(album -> {

            if (album.getStatus() != ContentStatus.PUBLISHED) {
                throw new ContentException(ContentErrorCode.ALBUM_NOT_FOUND, "id " + album.getId());
            }

            if (!artists.containsKey(album.getArtistId())) {
                throw new ContentException(ContentErrorCode.ARTIST_NOT_FOUND, "id " + album.getArtistId());
            }

            String coverUrl = storageClient.publicUrl(properties.cover().bucket(), album.getCoverKey());
            return new AlbumResponse(album, artists.get(album.getArtistId()), coverUrl);
        }).toList();

        boolean mayHaveMore = !albums.isEmpty() && albums.size() > limit;

        Long nextCursor = (responses.size() == limit || mayHaveMore) ? lastExamined : null;
        return new CursorPage<>(responses, nextCursor);
    }

    /** Get a page of albums by a specific artist, newest first. */
    public CursorPage<AlbumResponse> listByArtist(Long artistId, Long cursor, int size) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());
        long lastExamined = cursor == null ? 0L : cursor;

        List<Album> albums = albumRepository.findByArtistIdAndStatusAndIdLessThanOrderByIdDesc(
                artistId, ContentStatus.PUBLISHED, Paging.startCursor(cursor), PageRequest.of(0, limit + 1));

        Artist artist = artistRepository.findByOwnerUserIdAndIsDeleted(artistId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ContentException(ContentErrorCode.ARTIST_NOT_FOUND, "id " + artistId));

        List<AlbumResponse> responses = albums.stream().map(album -> {
            String coverUrl = storageClient.publicUrl(properties.cover().bucket(), album.getCoverKey());
            return new AlbumResponse(album, artist, coverUrl);
        }).toList();

        boolean mayHaveMore = !albums.isEmpty() && albums.size() > limit;

        Long nextCursor = (responses.size() == limit || mayHaveMore) ? lastExamined : null;
        return new CursorPage<>(responses, nextCursor);
    }
}
