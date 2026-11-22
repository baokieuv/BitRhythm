package com.bit_rhythm.content_service.search;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contents/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> search(@RequestParam String q,
                                                            @RequestParam(defaultValue = "TRACK") SearchType type,
                                                            @RequestParam(required = false) Long cursor,
                                                            @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, searchService.search(q, type, cursor, size));
    }
}
