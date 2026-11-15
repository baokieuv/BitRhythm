package com.bit_rhythm.content_service.search.dto;

import com.bit_rhythm.content_service.search.SearchType;

/** One result, whatever its type: enough to draw a row and open the page of the thing. subtitle = artist name (track, album) or null (artist). */
public record SearchHit(SearchType type, Long id, String title, String subtitle, String coverUrl) {
}
