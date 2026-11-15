package com.bit_rhythm.content_service.event;

import java.time.Instant;
import java.util.Map;

/**
 * Domain event of the catalog, published on content.topics.content-events (JSON). Same envelope as the events of the other services:
 * only ADD fields, bump "version" for breaking changes. Consumers: user-service (it knows the followers of an artist and can turn
 * TRACK_PUBLISHED into one NEW_RELEASE notification per follower through notification-service).
 *
 * <pre>
 * { "eventId": "track-published:123", "eventType": "TRACK_PUBLISHED", "version": 1, "occurredAt": "...",
 *   "data": { "trackId": 123, "title": "...", "artistId": 7, "artistName": "...", "artistUserId": 42 },
 *   "metadata": { "source": "content-service", "correlationId": null } }
 * </pre>
 *
 * eventId is deterministic ("track-published:{trackId}"): the first publication only; a track published again is the same fact.
 */
public record ContentEvent(
        String eventId,
        String eventType,
        int version,
        Instant occurredAt,
        Map<String, Object> data,
        Metadata metadata) {

    public record Metadata(String source, String correlationId) {
    }
}
