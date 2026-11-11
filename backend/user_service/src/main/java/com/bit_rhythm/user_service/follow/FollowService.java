package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.block.BlockService;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.common.Paging;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.config.UserProperties;
import com.bit_rhythm.user_service.event.UserEventPublisher;
import com.bit_rhythm.user_service.follow.dto.FollowUserResponse;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.ProfileStatus;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.settings.SettingsRepository;
import com.bit_rhythm.user_service.settings.UserSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private static final String DEFAULT_LOCALE = "en";

    private final FollowRepository followRepository;
    private final BlockService blockService;
    private final ProfileRepository profileRepository;
    private final SettingsRepository settingsRepository;
    private final UserEventPublisher userEventPublisher;
    private final StorageClient storageClient;
    private final UserProperties properties;
    private final TransactionTemplate transactionTemplate;

    /** [S1] FollowRepository.saveAndFlush(...); adjust both counters (+1) only when it returned 1. Publish event to outbox. */
    public void follow(Long followerId, Long followeeId) {

        if (Objects.equals(followerId, followeeId)) {
            throw new UserException(UserErrorCode.CANNOT_FOLLOW_SELF);
        }

        UserProfile followee = findActive(followeeId)
                .filter(profile -> profile.getStatus() == ProfileStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        UserProfile follower = findActive(followerId)
                .filter(profile -> profile.getStatus() == ProfileStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        if (blockService.isBlockedEitherWay(followerId, followeeId)) {
            throw new UserException(UserErrorCode.USER_BLOCKED);
        }

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return; // already following, do nothing
        }

        String followeeLocale = settingsRepository.findByUserIdAndIsDeleted(followeeId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .map(UserSettings::getLocale)
                .orElse(DEFAULT_LOCALE);

        try {
            transactionTemplate.executeWithoutResult(status -> {
                followRepository.saveAndFlush(new Follow(followerId, followeeId));   // flush: a duplicate fails HERE
                profileRepository.addFollowing(followerId, +1);
                profileRepository.addFollowers(followeeId, +1);

                userEventPublisher.publishNewFollower(follower, followeeId, followeeLocale);
            });
        } catch (DataIntegrityViolationException alreadyBlocked) {
            // identical request won the race: nothing left to do
        }
    }

    /** [S1] FollowRepository.deletePair(...); adjust both counters (-1) only when it returned 1. No event. */
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        if (followRepository.deletePair(followerId, followeeId) == 1) {
            profileRepository.addFollowing(followerId, -1);
            profileRepository.addFollowers(followeeId, -1);
        }
    }

    /** List people who follow the given user. */
    public CursorPage<FollowUserResponse> followers(Long userId, Long cursor, int size) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<Follow> rows = followRepository.findByFolloweeIdAndIdLessThanOrderByIdDesc(
                userId, Paging.startCursor(cursor), PageRequest.of(0, limit + 1));

        boolean hasNext = rows.size() > limit;
        List<Follow> pageRows = hasNext ? rows.subList(0, limit) : rows;

        Map<Long, UserProfile> profiles = profileRepository
                .findByUserIdIn(pageRows.stream().map(Follow::getFollowerId).toList()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<FollowUserResponse> items = pageRows.stream()
                .map(follow -> {
                    var p = profiles.get(follow.getFollowerId());
                    if (p == null || p.getStatus() != ProfileStatus.ACTIVE) {
                        return null;
                    }
                    return new FollowUserResponse(
                            p.getUserId(),
                            p.getDisplayName(),
                            storageClient.publicUrl(p.getAvatarKey()),
                            p.isVerified(),
                            follow.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        Long nextCursor = hasNext ? pageRows.getLast().getId() : null;
        return new CursorPage<>(items, nextCursor);
    }

    /** List people who are followed by the given user. */
    public CursorPage<FollowUserResponse> following(Long userId, Long cursor, int size) {
        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());

        List<Follow> rows = followRepository.findByFollowerIdAndIdLessThanOrderByIdDesc(
                userId, Paging.startCursor(cursor), PageRequest.of(0, limit + 1));

        boolean hasNext = rows.size() > limit;
        List<Follow> pageRows = hasNext ? rows.subList(0, limit) : rows;

        Map<Long, UserProfile> profiles = profileRepository
                .findByUserIdIn(pageRows.stream().map(Follow::getFolloweeId).toList()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<FollowUserResponse> items = pageRows.stream()
                .map(follow -> {
                    var p = profiles.get(follow.getFolloweeId());
                    if (p == null || p.getStatus() != ProfileStatus.ACTIVE) {
                        return null;
                    }
                    return new FollowUserResponse(
                            p.getUserId(),
                            p.getDisplayName(),
                            storageClient.publicUrl(p.getAvatarKey()),
                            p.isVerified(),
                            follow.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        Long nextCursor = hasNext ? pageRows.getLast().getId() : null;
        return new CursorPage<>(items, nextCursor);
    }

    /** existsByFollowerIdAndFolloweeId; used by ProfileService.getPublic. */
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    private Optional<UserProfile> findActive(Long userId) {
        return profileRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode());
    }

}
