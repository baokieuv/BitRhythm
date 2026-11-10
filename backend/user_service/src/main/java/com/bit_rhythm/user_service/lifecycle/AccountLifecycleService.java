package com.bit_rhythm.user_service.lifecycle;

import org.springframework.stereotype.Service;

@Service
public class AccountLifecycleService {

    /**
     * [S2] TODO(hint), one @Transactional method:
     *  1. if processed_events already has eventId -> return (duplicate); otherwise insert it FIRST so a crash re-runs everything;
     *  2. USER_DELETED -> onUserDeleted, USER_DISABLED -> onUserDisabled, anything else -> ignore.
     */
    public void handle(String eventId, String eventType, Long userId) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * [S2] TODO(hint): anonymize the profile (status DELETED, displayName "Deleted user", bio/country/avatarKey null), delete the avatar
     * object from MinIO AFTER the commit, and remove follows + blocks of the user. Before deleting the follows, correct the counters
     * of the OTHER users: for every row where the user is the follower, addFollowers(followee, -1); where the user is the followee,
     * addFollowing(follower, -1) (FollowRepository.findByFollowerId / findByFolloweeId, then deleteAllOf).
     */
    public void onUserDeleted(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S2] TODO(hint): disabled accounts are hidden, not erased: status HIDDEN; reactivation (if you ever add it) sets ACTIVE again. */
    public void onUserDisabled(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }
}
