package com.bit_rhythm.user_service.artist;

import org.springframework.stereotype.Service;

/**
 * [S2] submit (chỉ 1 PENDING/người, kiểm tra trong service), myStatus, listPending, approve, reject.
 * approve idempotent; thứ tự: AuthClient.grantRole trước, rồi cập nhật DB + verified = true;
 * sau đó phát thông báo kết quả (thêm 2 loại vào NotificationType của notification-service).
 */
@Service
public class ArtistVerificationService {
    // TODO [S2]
}
