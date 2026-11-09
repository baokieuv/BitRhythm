package com.bit_rhythm.user_service.event.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserOutboxRepository extends JpaRepository<UserOutboxEntity, Long> {
    List<UserOutboxEntity> findTop50ByStatusOrderByIdAsc(OutboxStatus status);
}
