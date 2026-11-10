package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.block.BlockRepository;
import com.bit_rhythm.user_service.block.UserBlock;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.follow.dto.FollowUserResponse;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final ProfileRepository profileRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * [S1] TODO(hint), ONE @Transactional method:
     *  1. followerId == followeeId -> CANNOT_FOLLOW_SELF; the target must have a profile (PROFILE_NOT_FOUND). Note: a user who never
     *     called this service has no profile yet; either accept the 404 or create profiles from an auth-service event.
     *  2. [S2] blocked either way -> USER_BLOCKED.
     *  3. already following -> do nothing (idempotent, no error, no second notification).
     *  4. insert Follow; ProfileRepository.addFollowers(followee, +1) and addFollowing(follower, +1);
     *  5. UserEventPublisher.publishNewFollower(...) writes to the outbox in this same transaction.
     * Two requests at once: the unique key makes the second insert fail; catch DataIntegrityViolationException and treat it as "already following".
     */
    public void follow(Long followerId, Long followeeId) {

        if (Objects.equals(followerId, followeeId)) {
            throw new UserException(UserErrorCode.CANNOT_FOLLOW_SELF);
        }

        profileRepository.findByUserIdAndIsDeleted(followeeId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        if (blockRepository.existsEitherWay(followerId, followeeId)) {
            throw new UserException(UserErrorCode.USER_BLOCKED);
        }

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return; // already following, do nothing
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                followRepository.saveAndFlush(new Follow(followerId, followeeId));   // flush: a duplicate fails HERE
                profileRepository.addFollowing(followerId, +1);
                profileRepository.addFollowers(followeeId, +1);
            });
        } catch (DataIntegrityViolationException alreadyBlocked) {
            // identical request won the race: nothing left to do
        }

        // publish event to outbox (in the same transaction)
    }

    /** [S1] FollowRepository.deletePair(...); adjust both counters (-1) only when it returned 1. No event. */
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        if (followRepository.deletePair(followerId, followeeId) == 1) {
            profileRepository.addFollowing(followerId, -1);
            profileRepository.addFollowers(followeeId, -1);
        }
    }

    /**
     * [S1] TODO(hint): cursor == null -> Long.MAX_VALUE; read size + 1 rows (size clamped to user.pagination.max-page-size);
     * load the profiles of the followers in ONE query (findByUserIdIn); nextCursor = id of the last row returned when there is one more.
     * [S2] hide HIDDEN/DELETED profiles and, for a PRIVATE profile, answer only to the owner or followers.
     */
    public CursorPage<FollowUserResponse> followers(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): same as followers, over findByFollowerIdAndIdLessThanOrderByIdDesc. */
    public CursorPage<FollowUserResponse> following(Long userId, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }

    /** existsByFollowerIdAndFolloweeId; used by ProfileService.getPublic. */
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }
}
