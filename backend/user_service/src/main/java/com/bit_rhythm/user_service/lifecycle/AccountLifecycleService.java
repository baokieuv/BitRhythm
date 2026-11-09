package com.bit_rhythm.user_service.lifecycle;

import org.springframework.stereotype.Service;

/**
 * [S2] onUserDeleted(id): status = DELETED, xóa bio, xóa ảnh MinIO, xóa follows + blocks,
 * điều chỉnh bộ đếm của người liên quan. onUserRegistered(id, email): tạo hồ sơ + settings sớm.
 */
@Service
public class AccountLifecycleService {
    // TODO [S2]
}
