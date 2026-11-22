package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.album.AlbumRepository;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.artist.ArtistService;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The ownership rule of the studio in one place: every studio method starts with artist(userId) and only touches tracks and
 * albums whose artistId is that artist's id. Something of somebody else answers *_NOT_FOUND (its existence is not revealed).
 */
@Component
@RequiredArgsConstructor
public class StudioOwnership {

    private final ArtistService artistService;
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;

    /** The artist page of the user, or ARTIST_PROFILE_REQUIRED. */
    Artist artist(Long userId) {
        return artistService.requireOwnedBy(userId);
    }

    Track track(Artist artist, Long trackId) {
        return trackRepository.findById(trackId)
                .filter(track -> track.getArtistId().equals(artist.getId()))
                .orElseThrow(() -> new ContentException(ContentErrorCode.TRACK_NOT_FOUND));
    }

    Album album(Artist artist, Long albumId) {
        return albumRepository.findById(albumId)
                .filter(album -> album.getArtistId().equals(artist.getId()))
                .orElseThrow(() -> new ContentException(ContentErrorCode.ALBUM_NOT_FOUND));
    }

}
