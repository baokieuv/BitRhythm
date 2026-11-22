package com.bit_rhythm.content_service.event.outbox;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface ContentOutboxRepository extends JpaRepository<ContentOutboxEntity, Long> {

    /** Oldest unpublished rows, locked; rows locked by another instance are skipped (lock.timeout -2 = SKIP LOCKED). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("SELECT e FROM ContentOutboxEntity e WHERE e.isProcessed = 0 ORDER BY e.eventId ASC")
    List<ContentOutboxEntity> findAndLockUnpublishedEvents(Pageable pageable);
}
