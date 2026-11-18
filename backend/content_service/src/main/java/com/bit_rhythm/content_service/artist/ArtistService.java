package com.bit_rhythm.content_service.artist;

import com.bit_rhythm.content_service.artist.dto.ArtistResponse;
import com.bit_rhythm.content_service.artist.dto.UpdateArtistRequest;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.core.common.CommonEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final StorageClient storageClient;
    private final ContentProperties properties;

    /** Get an artist by ID, or throw an exception if not found. */
    public ArtistResponse get(Long artistId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): the artist of the logged-in user, or ARTIST_PROFILE_REQUIRED when the user has none yet. Every studio service
     * calls this first: it is how "only the owner can edit" is enforced (a track/album belongs to artist.getId()).
     */
    public Artist requireOwnedBy(Long userId) {

        return artistRepository
                .findByOwnerUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new ContentException(ContentErrorCode.ARTIST_PROFILE_REQUIRED));
    }

    /** [S1] TODO(hint): same as requireOwnedBy, mapped. */
    public ArtistResponse mine(Long userId) {

        Artist artist = requireOwnedBy(userId);
        String imageUrl = storageClient.publicUrl(properties.cover().bucket(), artist.getImageKey());

        return ArtistResponse.from(artist, imageUrl);
    }

    /**
     * [S1] TODO(hint): upsert. First call creates the artist (name required), later calls update name/bio/imageKey. A new imageKey must start with
     * "covers/{userId}/" and exist in the cover bucket (stat); delete the previous object AFTER the update. Two first calls at once:
     * the unique owner_user_id fails the second insert; read the row again. When the NAME changes, remember that listings show the name
     * stored here (no other copy to update in this service).
     */
    public ArtistResponse saveMine(Long userId, UpdateArtistRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
