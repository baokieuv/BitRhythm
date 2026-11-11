package com.bit_rhythm.user_service.search;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.avatar.storage.StorageClient;
import com.bit_rhythm.user_service.block.BlockService;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.common.Paging;
import com.bit_rhythm.user_service.config.UserProperties;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.ProfileStatus;
import com.bit_rhythm.user_service.profile.UserProfile;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Search users by the beginning of their display name.
 *
 * Results are ordered by user id (stable, so the cursor is the id of the last row examined). Rows the viewer must not see
 * (the viewer, users in a block with the viewer) are filtered AFTER reading, so the database is read again, up to
 * MAX_ROUNDS times, to fill the page. Matching is done by the database collation: case-insensitive, and as accent-insensitive
 * as the column collation is.
 */
@Service
@RequiredArgsConstructor
public class UserSearchService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 100;
    private static final int MAX_ROUNDS = 3;
    private static final char LIKE_ESCAPE = '!';   // must match ESCAPE '!' in ProfileRepository.searchByNamePrefix

    private final ProfileRepository profileRepository;
    private final BlockService blockService;
    private final StorageClient storageClient;
    private final UserProperties properties;

    public CursorPage<UserSummary> search(Long viewerId, String q, Long cursor, int size) {
        String query = normalize(q);
        if (query.length() < MIN_QUERY_LENGTH) {
            return new CursorPage<>(List.of(), null);   // too short: a prefix of 1 letter matches half of the users
        }

        int limit = Paging.clampSize(size, properties.pagination().maxPageSize());
        String pattern = escapeLike(query);
        Integer notDeleted = CommonEnums.DeletedStatus.NOT_DELETED.getCode();

        List<UserSummary> found = new ArrayList<>();
        long lastExamined = cursor == null ? 0L : cursor;
        boolean mayHaveMore = true;

        for (int round = 0; round < MAX_ROUNDS && found.size() < limit && mayHaveMore; round++) {
            List<UserProfile> batch = profileRepository.searchByNamePrefix(
                    pattern, ProfileStatus.ACTIVE, notDeleted, lastExamined, PageRequest.of(0, limit));
            if (batch.isEmpty()) {
                mayHaveMore = false;
                break;
            }

            Set<Long> hidden = blockService.blockedEitherWayAmong(viewerId, batch.stream().map(UserProfile::getUserId).toList());
            for (UserProfile profile : batch) {
                if (found.size() == limit) {
                    break;   // page full: stop BEFORE this row, so the cursor stays on the last row really examined
                }
                lastExamined = profile.getUserId();
                if (profile.getUserId().equals(viewerId) || hidden.contains(profile.getUserId())) {
                    continue;
                }
                found.add(new UserSummary(profile.getUserId(), profile.getDisplayName(),
                        storageClient.publicUrl(profile.getAvatarKey()), profile.isVerified()));
            }
            mayHaveMore = batch.size() == limit;   // a full batch may be followed by more rows
        }

        // a full page or unread rows: the client asks again from the last row examined (the last page can come back empty)
        Long nextCursor = (found.size() == limit || mayHaveMore) ? lastExamined : null;
        return new CursorPage<>(found, nextCursor);
    }

    private static String normalize(String q) {
        String trimmed = q == null ? "" : q.trim();
        return trimmed.length() <= MAX_QUERY_LENGTH ? trimmed : trimmed.substring(0, MAX_QUERY_LENGTH);
    }

    /** % and _ typed by the user are letters, not wildcards (the query appends its own % at the end). */
    static String escapeLike(String value) {
        return value
                .replace(String.valueOf(LIKE_ESCAPE), "" + LIKE_ESCAPE + LIKE_ESCAPE)
                .replace("%", LIKE_ESCAPE + "%")
                .replace("_", LIKE_ESCAPE + "_");
    }
}