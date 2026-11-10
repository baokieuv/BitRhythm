package com.bit_rhythm.user_service.block;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.follow.FollowRepository;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final ProfileRepository profileRepository;
    private final FollowRepository followRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * ONE @Transactional method: no self-block; the target must exist; already blocked -> do nothing (idempotent);
     * insert UserBlock; remove the follows in both directions (FollowRepository.deleteBetween) and correct the counters only for the
     * follows that really existed (check what was there BEFORE deleting: existsByFollowerIdAndFolloweeId in both directions).
     */
    public void block(Long blockerId, Long blockedId) {

        if (blockerId.equals(blockedId)) {
            throw new UserException(UserErrorCode.CANNOT_BLOCK_SELF);
        }

        profileRepository.findByUserIdAndIsDeleted(blockedId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return; // already blocked, do nothing
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                blockRepository.saveAndFlush(new UserBlock(blockerId, blockedId));   // flush: a duplicate fails HERE
                removeFollow(blockerId, blockedId);
                removeFollow(blockedId, blockerId);
            });
        } catch (DataIntegrityViolationException alreadyBlocked) {
            // identical request won the race: nothing left to do
        }
    }

    /** BlockRepository.deletePair. Follows are NOT restored. */
    public void unblock(Long blockerId, Long blockedId) {
        blockRepository.deletePair(blockerId, blockedId);
    }

    /** [S2] TODO(hint): cursor pagination over findByBlockerIdAndIdLessThanOrderByIdDesc, profiles loaded in one query. */
    public CursorPage<UserSummary> blocked(Long blockerId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** BlockRepository.existsEitherWay; used by ProfileService, FollowService and UserSearchService. */
    public boolean isBlockedEitherWay(Long a, Long b) {
        return blockRepository.existsEitherWay(a, b);
    }

    private void removeFollow(Long followerId, Long followeeId) {
        if (followRepository.deletePair(followerId, followeeId) == 1) {
            profileRepository.addFollowing(followerId, -1);
            profileRepository.addFollowers(followeeId, -1);
        }
    }
}
