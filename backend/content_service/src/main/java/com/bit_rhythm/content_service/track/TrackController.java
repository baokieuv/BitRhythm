package com.bit_rhythm.content_service.track;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contents/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;
    private final ResponseFactory responseFactory;

    /** New releases, optionally of one genre. */
    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> list(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam(required = false) Long genreId,
                                                          @RequestParam(required = false) Long cursor,
                                                          @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, trackService.listNew(genreId, cursor, size, CurrentUser.id(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, trackService.get(id, CurrentUser.id(jwt)));
    }
}
