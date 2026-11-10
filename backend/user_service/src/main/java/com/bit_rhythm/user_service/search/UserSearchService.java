package com.bit_rhythm.user_service.search;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.block.BlockRepository;
import com.bit_rhythm.user_service.block.BlockService;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.ProfileStatus;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserSearchService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final ProfileRepository profileRepository;
    private final BlockRepository blockRepository;
    private final StorageClient storageClient;

    public CursorPage<UserSummary> search(Long viewerId, String q, Long cursor, int size) {

        // 1. Too-short queries return an empty page.
        String query = q == null ? "" : q.trim();
        if (query.length() < MIN_QUERY_LENGTH) {
            return new CursorPage<>(List.of(), null);
        }

        int pageSize = size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        long afterId = cursor == null ? 0L : cursor;

        // 2. Exclude the viewer and everybody blocked in either direction.
        //    The viewer is always in the set, so it is never empty (an empty NOT IN can misbehave).
//        Set<Long> excluded = new HashSet<>(blockRepository.findBlockedUserIdsBothWays(viewerId));
//        excluded.add(viewerId);

        // 3. Fetch size + 1 rows to know whether another page exists.
        List<UserProfile> rows = profileRepository
                .findByDisplayNameStartingWithIgnoreCaseAndStatusOrderByUserIdAsc(
                        query,
                        ProfileStatus.ACTIVE,
                        PageRequest.of(0, pageSize + 1)
                );

        boolean hasNext = rows.size() > pageSize;
        List<UserProfile> pageRows = hasNext ? rows.subList(0, pageSize) : rows;

        // 4. Map to UserSummary with the avatar public URL.
        List<UserSummary> items = pageRows.stream()
                .map(p -> UserSummary.from(p, storageClient.publicUrl(p.getAvatarKey())))
                .toList();

        Long nextCursor = hasNext ? pageRows.getLast().getUserId() : null;
        return new CursorPage<>(items, nextCursor);
    }
}
