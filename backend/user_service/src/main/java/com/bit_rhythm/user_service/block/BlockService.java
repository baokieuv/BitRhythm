package com.bit_rhythm.user_service.block;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.common.Paging;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.config.UserProperties;
import com.bit_rhythm.user_service.event.UserEventPublisher;
import com.bit_rhythm.user_service.follow.FollowRepository;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
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
public class BlockService {

    private final BlockRepository blockRepository;
    private final ProfileRepository profileRepository;
    private final FollowRepository followRepository;
    private final TransactionTemplate transactionTemplate;
    private final StorageClient storageClient;
    private final UserEventPublisher eventPublisher;
    private final UserProperties userProperties;

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
                eventPublisher.publishUserBlockedEvent(blockerId, blockedId, true);
            });
        } catch (DataIntegrityViolationException alreadyBlocked) {
            // identical request won the race: nothing left to do
        }
    }

    /** BlockRepository.deletePair. Follows are NOT restored. */
    @Transactional
    public void unblock(Long blockerId, Long blockedId) {
        eventPublisher.publishUserBlockedEvent(blockerId, blockedId, false);
        blockRepository.deletePair(blockerId, blockedId);
    }

    /** The users blocked by blockerId, most recent first. A blocked user whose profile no longer exists is left out. */
    public CursorPage<UserSummary> blocked(Long blockerId, Long cursor, int size) {
        int limit = Paging.clampSize(size, userProperties.pagination().maxPageSize());

        List<UserBlock> rows = blockRepository.findByBlockerIdAndIdLessThanOrderByIdDesc(
                blockerId, Paging.startCursor(cursor), PageRequest.of(0, limit + 1)
        );

        boolean hasNext = rows.size() > limit;
        List<UserBlock> pageRows = hasNext ? rows.subList(0, limit) : rows;

        Map<Long, UserProfile> profiles = profileRepository
                .findByUserIdIn(pageRows.stream().map(UserBlock::getBlockedId).toList()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<UserSummary> items = pageRows.stream()
                .map(block -> profiles.get(block.getBlockedId()))
                .filter(Objects::nonNull)
                .map(p -> UserSummary.from(p, storageClient.publicUrl(p.getAvatarKey())))
                .toList();

        Long nextCursor = hasNext ? pageRows.getLast().getId() : null;
        return new CursorPage<>(items, nextCursor);
    }

    /** BlockRepository.existsEitherWay; used by ProfileService, FollowService and UserSearchService. */
    public boolean isBlockedEitherWay(Long a, Long b) {
        return blockRepository.existsEitherWay(a, b);
    }

    /** Return the set of IDs that are blocked either way by the viewer among the given IDs. */
    public Set<Long> blockedEitherWayAmong(Long viewerId, Collection<Long> ids) {
        if (viewerId == null || ids == null || ids.isEmpty()) {
            return Set.of();
        }

        Set<Long> hidden = new HashSet<>(blockRepository.findBlockedIdsAmong(viewerId, ids));

        hidden.addAll(blockRepository.findBlockerIdsAmong(viewerId, ids));

        return hidden;
    }

    private void removeFollow(Long followerId, Long followeeId) {
        if (followRepository.deletePair(followerId, followeeId) == 1) {
            profileRepository.addFollowing(followerId, -1);
            profileRepository.addFollowers(followeeId, -1);
        }
    }
}
