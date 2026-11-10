package com.bit_rhythm.user_service.search;

import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.profile.dto.UserSummary;
import org.springframework.stereotype.Service;

@Service
public class UserSearchService {

    /**
     * [S2] TODO(hint): q shorter than 2 characters -> empty page. ProfileRepository.findByDisplayNameStartingWith...(q, ACTIVE, page),
     * size + 1 rows for the cursor (here the cursor is a user id, ascending). Remove the viewer and everybody who blocked the viewer
     * or was blocked by the viewer (BlockService). Map to UserSummary with the avatar public URL. Escape % and _ in q if you switch to LIKE.
     */
    public CursorPage<UserSummary> search(Long viewerId, String q, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }
}
