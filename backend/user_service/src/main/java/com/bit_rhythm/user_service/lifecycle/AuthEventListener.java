package com.bit_rhythm.user_service.lifecycle;

import org.springframework.stereotype.Component;

/**
 * [S2] Nhận event của auth-service (USER_REGISTERED, USER_DELETED, USER_DISABLED).
 * TODO:
 *  - @KafkaListener(topics = "...", groupId = "user-service")
 *  - cùng 1 transaction: insert processed_events (trùng thì bỏ qua) + gọi AccountLifecycleService
 *  - cần auth-service phát USER_DELETED/USER_DISABLED (hiện chưa có)
 */
@Component
public class AuthEventListener {}
