package com.bit_rhythm.content_service.artist.dto;

import com.bit_rhythm.content_service.artist.Artist;

public record ArtistResponse(Long id, Long ownerUserId, String name, String bio, String imageUrl) {

    /** imageUrl = StorageClient.publicUrl(coverBucket, artist.getImageKey()), null when there is no image. */
    public static ArtistResponse from(Artist artist, String imageUrl) {
        return new ArtistResponse(artist.getId(), artist.getOwnerUserId(), artist.getName(), artist.getBio(), imageUrl);
    }
}
