package com.bit_rhythm.content_service.event;

import com.bit_rhythm.content_service.artist.Artist;
import com.bit_rhythm.content_service.event.outbox.ContentOutboxEntity;
import com.bit_rhythm.content_service.event.outbox.ContentOutboxRepository;
import com.bit_rhythm.content_service.track.Track;
import com.bit_rhythm.core.outbox.EventSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes catalog events to the outbox. OutboxPollingWorker publishes them to Kafka later, so an event exists if and only if the
 * transaction that caused it was committed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentEventPublisher {

    static final String SOURCE = "content-service";
    static final String AGGREGATE_TRACK = "Track";
    static final String TRACK_PUBLISHED = "TRACK_PUBLISHED";

    private final ContentOutboxRepository outboxRepository;

    /**
     * Record "this track was published". MANDATORY: it must run inside the transaction of StudioTrackService.publish, so the
     * event is saved or rolled back together with the status change. The caller calls it for the FIRST publication only
     * (published_at was null); a track published again is the same fact, and the deterministic eventId lets consumers drop a
     * repeat anyway.
     *
     * aggregateId = the artist id (not the track id): Kafka keys by it, so all events of one artist stay in order.
     * A serialization failure is NOT swallowed: it rolls the publication back, because the event could not be recorded.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishTrackPublished(Track track, Artist artist) {

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("trackId", track.getId());
        data.put("title", track.getTitle());
        data.put("artistId", artist.getId());
        data.put("artistName", artist.getName());
        data.put("artistUserId", artist.getOwnerUserId());

        ContentEvent event = new ContentEvent(
                "track-published:" + track.getId(),
                TRACK_PUBLISHED,
                1,
                Instant.now(),
                data,
                new ContentEvent.Metadata(SOURCE, null));

        ContentOutboxEntity row = new ContentOutboxEntity();
        row.setAggregateType(AGGREGATE_TRACK);
        row.setAggregateId(String.valueOf(artist.getId()));
        row.setEventType(TRACK_PUBLISHED);
        row.setPayload(EventSerializer.serialize(event));
        row.setIsProcessed(0);
        row.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(row);
        log.debug("outbox: {} for track {}", TRACK_PUBLISHED, track.getId());
    }
}