package com.bit_rhythm.user_service.block;

import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
import org.springframework.stereotype.Service;

@Service
public class BlockService {

    /**
     * [S2] TODO(hint), ONE @Transactional method: no self-block; the target must exist; already blocked -> do nothing (idempotent);
     * insert UserBlock; remove the follows in both directions (FollowRepository.deleteBetween) and correct the counters only for the
     * follows that really existed (check what was there BEFORE deleting: existsByFollowerIdAndFolloweeId in both directions).
     */
    public void block(Long blockerId, Long blockedId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): BlockRepository.deletePair. Follows are NOT restored. */
    public void unblock(Long blockerId, Long blockedId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): cursor pagination over findByBlockerIdAndIdLessThanOrderByIdDesc, profiles loaded in one query. */
    public CursorPage<UserSummary> blocked(Long blockerId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): BlockRepository.existsEitherWay; used by ProfileService, FollowService and UserSearchService. */
    public boolean isBlockedEitherWay(Long a, Long b) {
        throw new UnsupportedOperationException("TODO");
    }
}
