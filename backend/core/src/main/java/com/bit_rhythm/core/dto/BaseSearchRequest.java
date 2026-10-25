package com.bit_rhythm.core.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class BaseSearchRequest {

    // Pagination (Default to page 1, 10 items per page)
    private int pageIndex = 1;
    private int pageSize = 30;

    // Sorting (Default to sorting by createdAt descending)
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    // General search keyword
    private String keyword;

    /**
     * Converts the search request parameters into a Spring Data Pageable object.
     * Note: Spring Data Pagination is 0-indexed, so we subtract 1 from pageIndex.
     */
    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.ASC.name().equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, sortBy);

        // Ensure page is not negative
        int page = Math.max(0, pageIndex - 1);

        return PageRequest.of(page, pageSize, sort);
    }
}