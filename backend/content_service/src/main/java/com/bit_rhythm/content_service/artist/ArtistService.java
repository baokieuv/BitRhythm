package com.bit_rhythm.content_service.artist;

import com.bit_rhythm.content_service.artist.dto.ArtistResponse;
import com.bit_rhythm.content_service.artist.dto.UpdateArtistRequest;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.storage.StoredObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * The artist page of a user with the role ARTIST.
 *
 * Do NOT put @Transactional on the methods of this class: saveMine relies on two things that need NO outer transaction. A duplicate
 * insert (two first calls at once) must fail in its own transaction and be followed by a fresh read; and the previous cover is deleted
 * from the object store only AFTER the database update has really been committed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistService {

    private static final String COVER_PREFIX = "covers/";

    private final ArtistRepository artistRepository;
    private final StorageClient storageClient;
    private final ContentProperties properties;

    /** Public artist page. */
    public ArtistResponse get(Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.ARTIST_NOT_FOUND, "id " + artistId));
        return toResponse(artist);
    }

    /**
     * The artist of the logged-in user, or ARTIST_PROFILE_REQUIRED when the user has none yet. Every studio service calls this first: it
     * is how "only the owner can edit" is enforced (a track or an album belongs to artist.getId()).
     */
    public Artist requireOwnedBy(Long userId) {
        return artistRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new ContentException(ContentErrorCode.ARTIST_PROFILE_REQUIRED, "user " + userId));
    }

    public ArtistResponse mine(Long userId) {
        return toResponse(requireOwnedBy(userId));
    }

    /**
     * Upsert: the first call creates the artist page, later calls change name, bio and (when imageKey is given) the image.
     * The new image is checked BEFORE anything is written; the old one is deleted AFTER the update.
     * Listings show the name stored here, there is no other copy to update in this service.
     */
    public ArtistResponse saveMine(Long userId, UpdateArtistRequest request) {
        String newImageKey = checkedImageKey(userId, request.imageKey());

        Artist artist = findOrCreate(userId, request.name().trim());
        String previousImageKey = artist.getImageKey();

        artist.setName(request.name().trim());
        artist.setBio(blankToNull(request.bio()));
        if (newImageKey != null) {
            artist.setImageKey(newImageKey);
        }
        Artist saved = artistRepository.save(artist);   // committed when it returns: there is no outer transaction

        if (newImageKey != null && previousImageKey != null && !previousImageKey.equals(newImageKey)) {
            deleteQuietly(previousImageKey);
        }
        return toResponse(saved);
    }

    // ------------------------------------------------------------------ internals

    /** Two first calls at once: the unique owner_user_id fails the second insert; its transaction is over, so the row of the first call is simply read again. */
    private Artist findOrCreate(Long userId, String name) {
        return artistRepository.findByOwnerUserId(userId).orElseGet(() -> {
            try {
                return artistRepository.saveAndFlush(new Artist(userId, name));
            } catch (DataIntegrityViolationException createdMeanwhile) {
                return artistRepository.findByOwnerUserId(userId).orElseThrow(() -> createdMeanwhile);
            }
        });
    }

    /**
     * null/blank = keep the current image. Otherwise the key must be one this user got from POST /studio/covers/upload-url
     * ("covers/{userId}/...": nobody can claim somebody else's object), the object must really exist and must respect the limits again:
     * the client could have uploaded anything to the presigned URL. An oversized or wrong-type object is deleted.
     */
    private String checkedImageKey(Long userId, String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String prefix = COVER_PREFIX + userId + "/";
        if (!key.startsWith(prefix) || key.length() > 255 || key.contains("..")) {
            throw new ContentException(ContentErrorCode.COVER_NOT_UPLOADED, "key outside " + prefix);
        }

        ContentProperties.Cover cover = properties.cover();
        StoredObject stored = storageClient.stat(cover.bucket(), key)
                .orElseThrow(() -> new ContentException(ContentErrorCode.COVER_NOT_UPLOADED, key));

        if (stored.size() > cover.maxBytes()) {
            deleteQuietly(key);
            throw new ContentException(ContentErrorCode.COVER_TOO_LARGE, stored.size() + " bytes");
        }
        if (stored.contentType() == null || cover.allowedTypes().stream()
                .noneMatch(type -> type.equalsIgnoreCase(stored.contentType().toLowerCase(Locale.ROOT)))) {
            deleteQuietly(key);
            throw new ContentException(ContentErrorCode.COVER_TYPE_NOT_ALLOWED, String.valueOf(stored.contentType()));
        }
        return key;
    }

    private ArtistResponse toResponse(Artist artist) {
        return ArtistResponse.from(artist, storageClient.publicUrl(properties.cover().bucket(), artist.getImageKey()));
    }

    /** An orphan file in the object store is harmless: never fail the request over it. */
    private void deleteQuietly(String key) {
        try {
            storageClient.delete(properties.cover().bucket(), key);
        } catch (RuntimeException e) {
            log.warn("Could not delete cover object {}: {}", key, e.toString());
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}