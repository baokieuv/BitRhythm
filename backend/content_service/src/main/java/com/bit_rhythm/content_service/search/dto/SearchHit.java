package com.bit_rhythm.content_service.search.dto;

import com.bit_rhythm.content_service.album.Album;
import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.search.SearchType;
import com.bit_rhythm.content_service.track.Track;

/** One result, whatever its type: enough to draw a row and open the page of the thing. subtitle = artist name (track, album) or null (artist). */
public record SearchHit(SearchType type, Long id, String title, String subtitle, String coverUrl) {

    public static SearchHit track(Track track, String artistName, String coverUrl) {
        return new SearchHit(SearchType.TRACK, track.getId(), track.getTitle(), artistName, coverUrl);
    }

    public static SearchHit album(Album album, String artistName, String coverUrl) {
        return new SearchHit(SearchType.ALBUM, album.getId(), album.getTitle(), artistName, coverUrl);
    }

    /** imageUrl = the artist's public image URL, or null when the artist has none. */
    public static SearchHit artist(Artist artist, String imageUrl) {
        return new SearchHit(SearchType.ARTIST, artist.getId(), artist.getName(), null, imageUrl);
    }
}