package com.bit_rhythm.content_service.track;

import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/** The listener's view of the catalog: only PUBLISHED tracks (an owner also sees their own drafts). */
@Service
public class TrackService {

    /**
     * [S1] TODO(hint): TRACK_NOT_FOUND when missing. A DRAFT/HIDDEN track is visible only to the owner's artist and admins: everybody
     * else gets TRACK_NOT_FOUND, not a 403.
     */
    public TrackResponse get(Long trackId, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): new releases, optionally of one genre (genreId null = all). Paging.clampSize(size, content.pagination.max-page-size),
     * Paging.startCursor(cursor), ask for size + 1 rows; nextCursor = id of the last row returned when there is one more. Map with toResponses.
     */
    public CursorPage<TrackResponse> listNew(Long genreId, Long cursor, int size, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): PUBLISHED tracks of one artist (the artist page), same pagination. */
    public CursorPage<TrackResponse> listByArtist(Long artistId, Long cursor, int size, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): the tracks of a PUBLISHED album in track-number order (the album itself must be visible to the viewer: see AlbumService.get). */
    public List<TrackResponse> listByAlbum(Long albumId, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): ONE query each for the artists (ArtistRepository.findByIdIn), albums (AlbumRepository.findByIdIn), genres
     * (GenreRepository.findByIdIn) and the viewer's likes (TrackLikeRepository) of the whole page, never one query per track.
     * coverUrl = track cover, else album cover, via StorageClient.publicUrl(coverBucket, key). Used by every list of tracks
     * (search, playlists, library, discovery): make it the single place that builds a TrackResponse.
     */
    public List<TrackResponse> toResponses(List<Track> tracks, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }
}
