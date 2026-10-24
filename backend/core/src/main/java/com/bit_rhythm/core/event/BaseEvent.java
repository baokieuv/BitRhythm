package com.bit_rhythm.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Base event class for Kafka messages.
 * Includes necessary metadata for deduplication, tracing, and ordering.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    // Unique ID for the event. Used as the primary key for idempotency checks (e.g., Idempotency Key).
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    // The type of the event (e.g., "USER_CREATED", "PAYMENT_PROCESSED").
    private String eventType;

    // The ID of the business entity this event relates to (e.g., userId, orderId).
    // Useful for Kafka partitioning and ordered processing.
    private String aggregateId;

    // When the event was generated.
    @Builder.Default
    private Instant timestamp = Instant.now();

    // For optimistic locking or ordering (e.g., processing v2 only if v1 is processed).
    @Builder.Default
    private Integer version = 1;

    // Distributed tracing ID to track the flow across multiple microservices.
    private String traceId;

    // The service or system that published this event.
    private String source;
}