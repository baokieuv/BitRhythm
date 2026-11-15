package com.bit_rhythm.content_service.stream;

import com.bit_rhythm.content_service.stream.dto.StreamResponse;
import org.springframework.stereotype.Service;

@Service
public class StreamService {

    /**
     * [S1] TODO(hint): TRACK_NOT_FOUND unless the track is PUBLISHED with audioStatus READY (the owner's artist and admins may also listen to
     * their own DRAFT: that is how an artist checks an upload before publishing). Then
     * StorageClient.presignDownload(audioBucket, track.getAudioKey(), content.audio.stream-ttl) and expiresAt = now + ttl.
     * The audio bucket must be PRIVATE: with a public bucket this check is useless. This call does NOT count a play: the client calls
     * POST /tracks/{id}/play once the listener has really listened (PlayService).
     */
    public StreamResponse streamUrl(Long trackId, Long viewerId) {
        throw new UnsupportedOperationException("TODO");
    }
}
