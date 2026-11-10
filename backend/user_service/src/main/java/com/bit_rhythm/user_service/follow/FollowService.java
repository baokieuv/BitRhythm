package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.follow.dto.FollowUserResponse;
import org.springframework.stereotype.Service;

@Service
public class FollowService {

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
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): FollowRepository.deletePair(...); adjust both counters (-1) only when it returned 1. No event. */
    public void unfollow(Long followerId, Long followeeId) {
        throw new UnsupportedOperationException("TODO");
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

    /** [S1] TODO(hint): existsByFollowerIdAndFolloweeId; used by ProfileService.getPublic. */
    public boolean isFollowing(Long followerId, Long followeeId) {
        throw new UnsupportedOperationException("TODO");
    }
}
