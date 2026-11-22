package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.storage.StoredObject;
import com.bit_rhythm.content_service.studio.dto.UploadUrlRequest;
import com.bit_rhythm.content_service.studio.dto.UploadUrlResponse;
import com.bit_rhythm.content_service.track.AudioStatus;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackRepository;
import com.bit_rhythm.content_service.track.TrackService;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bit_rhythm.content_service.studio.CoverKeyChecker.normalizeType;

/**
 * Direct-to-MinIO uploads: the bytes never pass through this service. Object keys are chosen HERE, never from user input.
 * Not @Transactional: the object store has no transaction, and nothing here needs more than one row update.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Map<String, String> AUDIO_EXTENSIONS = Map.of(
            "audio/mpeg", "mp3",
            "audio/flac", "flac",
            "audio/aac", "aac",
            "audio/mp4", "m4a",
            "audio/x-wav", "wav");

    private static final Map<String, String> COVER_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final StudioOwnership ownership;
    private final TrackRepository trackRepository;
    private final TrackService trackService;
    private final StorageClient storageClient;
    private final AudioMetadataReader audioMetadataReader;
    private final ContentProperties properties;

    /**
     * Issue a signed PUT URL for the audio of a track. The key "audio/{trackId}/{uuid}.{ext}" is saved on the track with audioStatus
     * PENDING_UPLOAD; a second request replaces it and the previous object is deleted AFTER the row is updated.
     * Note: asking for a new upload on a track that is already PUBLISHED makes its audio PENDING_UPLOAD (not streamable) until the
     * new file is confirmed.
     */
    public UploadUrlResponse createAudioUploadUrl(Long userId, Long trackId, UploadUrlRequest request) {

        Artist artist = ownership.artist(userId);
        Track track = ownership.track(artist, trackId);

        String contentType = normalizeType(request.contentType());
        String extension = AUDIO_EXTENSIONS.get(contentType);
        if (extension == null || !properties.audio().allowedTypes().contains(contentType)) {
            throw new ContentException(ContentErrorCode.AUDIO_TYPE_NOT_ALLOWED);
        }
        if (request.sizeBytes() > properties.audio().maxBytes()) {
            throw new ContentException(ContentErrorCode.AUDIO_TOO_LARGE);
        }

        String key = "audio/" + trackId + "/" + java.util.UUID.randomUUID() + "." + extension;
        String previousKey = track.getAudioKey();

        track.setAudioKey(key);
        track.setAudioStatus(AudioStatus.PENDING_UPLOAD);
        track.setAudioContentType(null);
        track.setAudioSizeBytes(null);
        track.setDurationSeconds(null);
        track.setBitrateKbps(null);
        trackRepository.save(track);

        deleteQuietly(properties.audio().bucket(), previousKey);

        Duration ttl = properties.audio().uploadTtl();
        String url = storageClient.presignUpload(properties.audio().bucket(), key, contentType, ttl);
        return UploadUrlResponse.of(key, url, ttl);
    }

    /**
     * The client says "I uploaded it". The key is the one stored on the track. We check that the object exists and that its size and
     * type respect the limits AGAIN (a signed URL accepts anything), then read the real duration and bitrate from the file.
     * Not valid audio: the object is deleted, the track stays PENDING_UPLOAD without a key, and the answer is AUDIO_TYPE_NOT_ALLOWED.
     * Idempotent when the audio is already READY.
     */
    public TrackResponse confirmAudio(Long userId, Long trackId) {

        Artist artist = ownership.artist(userId);
        Track track = ownership.track(artist, trackId);

        if (track.getAudioStatus() == AudioStatus.READY) {
            return trackService.toResponses(List.of(track), userId).getFirst();
        }

        String bucket = properties.audio().bucket();
        String key = track.getAudioKey();
        if (key == null) {
            throw new ContentException(ContentErrorCode.AUDIO_NOT_UPLOADED);
        }

        StoredObject object = storageClient.stat(bucket, key)
                .orElseThrow(() -> new ContentException(ContentErrorCode.AUDIO_NOT_UPLOADED));

        String contentType = normalizeType(object.contentType());
        String extension = AUDIO_EXTENSIONS.get(contentType);

        if (object.size() > properties.audio().maxBytes()) {
            discardUpload(track, bucket, key);
            throw new ContentException(ContentErrorCode.AUDIO_TOO_LARGE);
        }
        if (extension == null || !properties.audio().allowedTypes().contains(contentType)) {
            discardUpload(track, bucket, key);
            throw new ContentException(ContentErrorCode.AUDIO_TYPE_NOT_ALLOWED);
        }

        // a storage failure here propagates as an error (retry later); only "not audio" discards the file
        var info = audioMetadataReader.read(bucket, key, extension).orElse(null);
        if (info == null) {
            discardUpload(track, bucket, key);
            throw new ContentException(ContentErrorCode.AUDIO_TYPE_NOT_ALLOWED);
        }

        track.setAudioContentType(contentType);
        track.setAudioSizeBytes(object.size());
        track.setDurationSeconds(info.durationSeconds());
        track.setBitrateKbps(info.bitrateKbps());
        track.setAudioStatus(AudioStatus.READY);
        trackRepository.save(track);

        return trackService.toResponses(java.util.List.of(track), userId).get(0);
    }

    /**
     * A signed PUT URL for a cover (track, album or artist image). The caller must be an artist (ARTIST_PROFILE_REQUIRED).
     * Nothing is saved: the key is sent back in TrackRequest / AlbumRequest / UpdateArtistRequest, where CoverKeyChecker checks the
     * folder and stat() before the key is used.
     */
    public UploadUrlResponse createCoverUploadUrl(Long userId, UploadUrlRequest request) {
        ownership.artist(userId);

        String contentType = normalizeType(request.contentType());
        String extension = COVER_EXTENSIONS.get(contentType);
        if (extension == null || !properties.cover().allowedTypes().contains(contentType)) {
            throw new ContentException(ContentErrorCode.COVER_TYPE_NOT_ALLOWED);
        }
        if (request.sizeBytes() > properties.cover().maxBytes()) {
            throw new ContentException(ContentErrorCode.COVER_TOO_LARGE);
        }

        String key = "covers/" + userId + "/" + UUID.randomUUID() + "." + extension;
        Duration ttl = properties.cover().uploadTtl();
        String url = storageClient.presignUpload(properties.cover().bucket(), key, contentType, ttl);
        return UploadUrlResponse.of(key, url, ttl);
    }

//    public void confirmCover(Long userId, String key) {
//        ownership.artist(userId);
//        if (key == null || !key.startsWith("covers/" + userId + "/") || key.contains("..")) {
//            throw new ContentException(ContentErrorCode.COVER_NOT_UPLOADED);
//        }
//        StoredObject object = storageClient.stat(properties.cover().bucket(), key)
//                .orElseThrow(() -> new ContentException(ContentErrorCode.COVER_NOT_UPLOADED));
//
//        String contentType = normalizeType(object.contentType());
//        String extension = COVER_EXTENSIONS.get(contentType);
//        if (object.size() > properties.cover().maxBytes()) {
//            deleteQuietly(properties.cover().bucket(), key);
//            throw new ContentException(ContentErrorCode.COVER_TOO_LARGE);
//        }
//        if (extension == null || !properties.cover().allowedTypes().contains(contentType)) {
//            deleteQuietly(properties.cover().bucket(), key);
//            throw new ContentException(ContentErrorCode.COVER_TYPE_NOT_ALLOWED);
//        }
//    }

    /** Rejected upload: delete the object and forget the key, so the artist must ask for a new URL. */
    private void discardUpload(Track track, String bucket, String key) {
        track.setAudioKey(null);
        track.setAudioStatus(AudioStatus.PENDING_UPLOAD);
        trackRepository.save(track);
        deleteQuietly(bucket, key);
    }

    /** The object store is not transactional: a failed delete never fails the request. */
    private void deleteQuietly(String bucket, String key) {
        if (key == null) {
            return;
        }
        try {
            storageClient.delete(bucket, key);
        } catch (Exception e) {
            log.warn("could not delete object {}/{}: {}", bucket, key, e.toString());
        }
    }
}
