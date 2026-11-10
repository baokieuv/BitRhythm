package com.bit_rhythm.user_service.artist;

import com.bit_rhythm.user_service.artist.dto.response.VerificationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArtistVerificationService {

    /**
     * [S2] TODO(hint): the profile must exist; already verified -> ALREADY_VERIFIED; existsByUserIdAndStatus(PENDING) ->
     * VERIFICATION_ALREADY_PENDING (there is no partial unique index in MariaDB: check inside the transaction); save PENDING.
     */
    public VerificationResponse submit(Long userId, String message) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): latest request of the user, VERIFICATION_NOT_FOUND when there is none. */
    public VerificationResponse mine(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): PENDING requests, oldest first, page size <= 100. */
    public List<VerificationResponse> pending(int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint): the request must be PENDING (idempotent: APPROVED -> return it as it is). Order matters:
     *  1. AuthClient.grantRole(userId, "ARTIST") FIRST (idempotent, safe to repeat after a failure);
     *  2. then in one transaction: request APPROVED + reviewedBy/reviewedAt/note, profile.verified = true;
     *  3. then an outbox event for notification-service (add ARTIST_VERIFICATION_APPROVED to its NotificationType + templates).
     * If step 2 fails after step 1, the admin simply clicks approve again.
     */
    public VerificationResponse approve(Long requestId, Long adminId, String note) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): PENDING -> REJECTED with note; outbox event ARTIST_VERIFICATION_REJECTED (new NotificationType). */
    public VerificationResponse reject(Long requestId, Long adminId, String note) {
        throw new UnsupportedOperationException("TODO");
    }
}
