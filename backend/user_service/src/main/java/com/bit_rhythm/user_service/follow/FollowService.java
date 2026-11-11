package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.block.BlockRepository;
import com.bit_rhythm.user_service.block.BlockService;
import com.bit_rhythm.user_service.block.UserBlock;
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
import com.bit_rhythm.user_service.settings.ProfileVisibility;
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

    private Optional<UserProfile> findActive(Long userId) {
        return profileRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode());
    }

    /** true: show the lists; false: show an empty page; throws PROFILE_NOT_FOUND when the owner must look non-existent. */
    private boolean canSeeLists(Long viewerId, Long ownerId) {
        UserProfile owner = findActive(ownerId)
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));
        if (Objects.equals(viewerId, ownerId)) {
            return true;
        }
        if (owner.getStatus() != ProfileStatus.ACTIVE || blockService.isBlockedEitherWay(viewerId, ownerId)) {
            throw new UserException(UserErrorCode.PROFILE_NOT_FOUND);
        }
        boolean isPrivate = settingsRepository.findById(ownerId)
                .map(UserSettings::getVisibility)
                .filter(visibility -> visibility == ProfileVisibility.PRIVATE)
                .isPresent();
        return !isPrivate || followRepository.existsByFollowerIdAndFolloweeId(viewerId, ownerId);
    }

    /** rows holds up to limit + 1 follows: the extra one only tells that another page exists. */
    private CursorPage<FollowUserResponse> toPage(Long viewerId, List<Follow> rows, int limit,
                                                  Function<Follow, Long> otherUserId) {
        boolean hasNext = rows.size() > limit;
        List<Follow> page = hasNext ? rows.subList(0, limit) : rows;

        List<Long> ids = page.stream().map(otherUserId).toList();
        Map<Long, UserProfile> profiles = profileRepository.findByUserIdIn(ids).stream()   // ONE query for the whole page
                .filter(profile -> profile.getStatus() == ProfileStatus.ACTIVE)
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        Set<Long> hiddenFromViewer = blockService.blockedEitherWayAmong(viewerId, ids);

        List<FollowUserResponse> items = page.stream()
                .filter(follow -> !hiddenFromViewer.contains(otherUserId.apply(follow)))
                .map(follow -> {
                    UserProfile profile = profiles.get(otherUserId.apply(follow));
                    return profile == null ? null : new FollowUserResponse(profile.getUserId(), profile.getDisplayName(),
                            storageClient.publicUrl(profile.getAvatarKey()), profile.isVerified(), follow.getCreatedAt());
                })
                .filter(Objects::nonNull)
                .toList();

        // the cursor is the id of the last follow row of this page (rows left out above still count)
        Long nextCursor = hasNext ? page.getLast().getId() : null;
        return new CursorPage<>(items, nextCursor);
    }
}
