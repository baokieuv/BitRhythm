package com.bit_rhythm.user_service.event.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserOutboxRepository extends JpaRepository<UserOutboxEntity, Long> {
    @Query(value = """
        SELECT * FROM user_service.outbox 
        WHERE is_processed = 0 
        ORDER BY created_at ASC 
        LIMIT 100 
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<UserOutboxEntity> findAndLockUnpublishedEvents();
}
