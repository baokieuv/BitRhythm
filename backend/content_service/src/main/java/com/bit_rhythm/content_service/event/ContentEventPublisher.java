package com.bit_rhythm.content_service.event;

import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.track.Track;
import org.springframework.stereotype.Service;

@Service
public class ContentEventPublisher {

    /**
     * [S2] TODO(hint): @Transactional(propagation = MANDATORY): call it inside the transaction of StudioTrackService.publish (first publication only),
     * so the event exists if and only if the publication was committed. Build the ContentEvent (data: trackId, title, artistId, artistName,
     * artistUserId = artist.getOwnerUserId()), serialize it with core's EventSerializer and save a ContentOutboxEntity
     * (aggregateType "Track", aggregateId = artist id so the events of one artist stay in order, eventType "TRACK_PUBLISHED").
     * See UserEventPublisher of user-service for the same pattern.
     */
    public void publishTrackPublished(Track track, Artist artist) {
        throw new UnsupportedOperationException("TODO");
    }
}
