package com.bit_rhythm.content_service.album;

import com.bit_rhythm.content_service.album.dto.AlbumResponse;
import com.bit_rhythm.content_service.common.CursorPage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbumService {

    /**
     * [S1] TODO(hint): ALBUM_NOT_FOUND when missing. A DRAFT/HIDDEN album is visible only to its owner (and admins): everybody else gets
     * ALBUM_NOT_FOUND, not a 403 (do not reveal that it exists).
     */
    public AlbumResponse get(Long albumId, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): published albums, newest first; Paging.clampSize/startCursor; size + 1 rows; map with toResponses. */
    public CursorPage<AlbumResponse> listPublished(Long cursor, int size, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): published albums of one artist (artist page). */
    public CursorPage<AlbumResponse> listByArtist(Long artistId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): ONE query for the artists of the page (ArtistRepository.findByIdIn) and ONE for the viewer's saved albums
     * (SavedAlbumRepository); coverUrl = StorageClient.publicUrl(coverBucket, coverKey). Never one query per album.
     */
    public List<AlbumResponse> toResponses(List<Album> albums, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }
}
