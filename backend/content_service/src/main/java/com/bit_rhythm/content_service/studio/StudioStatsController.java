package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ARTIST only (SecurityConfig). */
@RestController
@RequestMapping("/api/contents/studio/stats")
@RequiredArgsConstructor
public class StudioStatsController {

    private final StudioStatsService studioStatsService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> stats(@AuthenticationPrincipal Jwt jwt) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioStatsService.stats(CurrentUser.id(jwt)));
    }
}
