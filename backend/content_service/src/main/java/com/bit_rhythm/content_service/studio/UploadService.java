package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.content_service.studio.dto.UploadUrlRequest;
import com.bit_rhythm.content_service.studio.dto.UploadUrlResponse;
import com.bit_rhythm.content_service.track.dto.TrackResponse;
import org.springframework.stereotype.Service;

/** Direct-to-MinIO uploads: the bytes never pass through this service. */
@Service
public class UploadService {

    /**
     * [S1] TODO(hint): the track must belong to the artist of userId. contentType must be in content.audio.allowed-types
     * (AUDIO_TYPE_NOT_ALLOWED), sizeBytes <= content.audio.max-bytes (AUDIO_TOO_LARGE). The key is chosen HERE:
     * "audio/" + trackId + "/" + UUID + "." + extension from the content type (never from user input). Save it in track.audioKey with
     * audioStatus PENDING_UPLOAD (a second request replaces it: delete the previous object), then
     * StorageClient.presignUpload(audioBucket, key, contentType, content.audio.upload-ttl).
     */
    public UploadUrlResponse createAudioUploadUrl(Long userId, Long trackId, UploadUrlRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): no body: the key is the one stored on the track. StorageClient.stat(audioBucket, key) must exist
     * (AUDIO_NOT_UPLOADED) and its size / content type must respect the limits again (the client could have uploaded anything).
     * Then read the real duration and bitrate FROM THE FILE (ranged GET of the header + an audio-tag library; never trust numbers sent by
     * the client), store audioSizeBytes, audioContentType, durationSeconds, bitrateKbps and set audioStatus READY. If the file is not
     * valid audio: delete the object, keep PENDING_UPLOAD and answer AUDIO_TYPE_NOT_ALLOWED. Idempotent when already READY.
     */
    public TrackResponse confirmAudio(Long userId, Long trackId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S1] TODO(hint): for track, album and artist images. The user must be an artist (ARTIST_PROFILE_REQUIRED). Same checks with
     * content.cover.* (COVER_TYPE_NOT_ALLOWED, COVER_TOO_LARGE); key = "covers/" + userId + "/" + UUID + "." + extension.
     * Nothing is saved yet: the key is sent back in TrackRequest / AlbumRequest / UpdateArtistRequest, where the service checks the
     * prefix and stat() before using it.
     */
    public UploadUrlResponse createCoverUploadUrl(Long userId, UploadUrlRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
