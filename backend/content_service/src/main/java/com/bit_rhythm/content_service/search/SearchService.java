package com.bit_rhythm.content_service.search;

import com.bit_rhythm.content_service.common.CursorPage;
import com.bit_rhythm.content_service.search.dto.SearchHit;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    /**
     * [S1] TODO(hint): q trimmed, shorter than 2 characters -> empty page; at most 100 characters. Per type:
     *  - TRACK: FULLTEXT. Build the boolean query from the words ("+word*" for each, after stripping + - ~ < > ( ) " @ * from the input) and call
     *    TrackRepository.searchPublished(query, Paging.startCursor(cursor), size + 1);
     *  - ALBUM: AlbumRepository.searchPublishedByTitlePrefix(escapeLike(q), cursor, Pageable) (escape '!', % and _);
     *  - ARTIST: ArtistRepository.searchByNamePrefix(escapeLike(q), cursor, Pageable).
     * size + 1 rows for the cursor; artist names and covers of a page in ONE query each (never per row).
     */
    public CursorPage<SearchHit> search(String q, SearchType type, Long cursor, int size) {
        throw new UnsupportedOperationException("TODO");
    }
}
