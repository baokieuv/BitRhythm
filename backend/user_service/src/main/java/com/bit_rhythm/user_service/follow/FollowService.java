package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.common.exception.UserCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.event.UserEventPublisher;
import com.bit_rhythm.user_service.follow.dto.FollowUserResponse;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.UserProfile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private static final int MAX_SIZE = 50;

    private final FollowRepository follows;
    private final ProfileRepository profiles;
    private final UserEventPublisher events;
    private final StorageClient storage;

    public FollowService(FollowRepository follows, ProfileRepository profiles,
                         UserEventPublisher events, StorageClient storage) {
        this.follows = follows;
        this.profiles = profiles;
        this.events = events;
        this.storage = storage;
    }

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) throw new UserException(UserCode.CANNOT_FOLLOW_SELF);
        // [S2] if (blockService.isBlockedEitherWay(followerId, followeeId)) throw USER_BLOCKED

        UserProfile follower = profiles.findById(followerId)
                .orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));
        UserProfile followee = profiles.findById(followeeId)
                .orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));

        if (follows.existsByFollowerIdAndFolloweeId(followerId, followeeId)) return; // idempotent

        follows.save(new Follow(followerId, followeeId));
        profiles.adjustFollowingCount(followerId, 1);
        profiles.adjustFollowerCount(followeeId, 1);
        events.publishNewFollower(follower, followee);
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        int deleted = follows.deleteRelation(followerId, followeeId);
        if (deleted > 0) {                       // chỉ trừ bộ đếm khi thật sự xóa được dòng
            profiles.adjustFollowingCount(followerId, -1);
            profiles.adjustFollowerCount(followeeId, -1);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long a, Long b) {
        return follows.existsByFollowerIdAndFolloweeId(a, b);
    }

    @Transactional(readOnly = true)
    public CursorPage<FollowUserResponse> followers(Long userId, Long cursor, int size) {
        return page(follows.findFollowers(userId, normCursor(cursor), PageRequest.of(0, normSize(size) + 1)),
                size, Follow::getFollowerId);
    }

    @Transactional(readOnly = true)
    public CursorPage<FollowUserResponse> following(Long userId, Long cursor, int size) {
        return page(follows.findFollowing(userId, normCursor(cursor), PageRequest.of(0, normSize(size) + 1)),
                size, Follow::getFolloweeId);
    }

    private CursorPage<FollowUserResponse> page(List<Follow> rows, int size, Function<Follow, Long> userIdOf) {
        int limit = normSize(size);
        boolean hasMore = rows.size() > limit;
        var pageRows = hasMore ? rows.subList(0, limit) : rows;

        Map<Long, UserProfile> byId = profiles.findAllById(pageRows.stream().map(userIdOf).toList()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        var items = pageRows.stream().map(userIdOf).map(byId::get)
                .filter(p -> p != null)
                .map(p -> new FollowUserResponse(p.getUserId(), p.getDisplayName(),
                        storage.publicUrl(p.getAvatarKey()), p.isVerified()))
                .toList();
        Long next = hasMore ? pageRows.get(pageRows.size() - 1).getId() : null;
        return new CursorPage<>(items, next);
    }

    private static Long normCursor(Long c) { return c == null ? Long.MAX_VALUE : c; }
    private static int normSize(int s) { return Math.max(1, Math.min(s, MAX_SIZE)); }
}
