package com.bit_rhythm.content_service.stream;

import com.bit_rhythm.content_service.common.ContentStatus;
import com.bit_rhythm.content_service.common.exception.ContentErrorCode;
import com.bit_rhythm.content_service.common.exception.ContentException;
import com.bit_rhythm.content_service.config.ContentProperties;
import com.bit_rhythm.content_service.storage.StorageClient;
import com.bit_rhythm.content_service.stream.dto.StreamResponse;
import com.bit_rhythm.content_service.track.AudioStatus;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.content_service.track.TrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Hands out a short-lived signed URL for the audio of a track; the bytes go straight from the (PRIVATE) audio bucket to the client.
 * With a public bucket the visibility check here would be useless: keep the bucket private.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamService {

    private final TrackService trackService;
    private final StorageClient storageClient;
    private final ContentProperties properties;

    /**
     * TRACK_NOT_FOUND unless the track is visible to the viewer (PUBLISHED, or the viewer is its artist, who may listen to their own
     * DRAFT to check an upload before publishing; blocks apply) AND its audio is READY. A TAKEN_DOWN track is never streamed, not even
     * to its owner. This call does NOT count a play: the client calls POST /tracks/{id}/play once the listener has really listened.
     */
    public StreamResponse streamUrl(Long trackId, Long viewerId) {

        Track track = trackService.findVisible(trackId, viewerId);

        if (track.getStatus() == ContentStatus.TAKEN_DOWN
                || track.getAudioStatus() != AudioStatus.READY
                || track.getAudioKey() == null) {
            throw new ContentException(ContentErrorCode.TRACK_NOT_FOUND);
        }

        Duration ttl = properties.audio().streamTtl();
        String url = storageClient.presignDownload(properties.audio().bucket(), track.getAudioKey(), ttl);

        return new StreamResponse(url, Instant.now().plus(ttl));
    }
}