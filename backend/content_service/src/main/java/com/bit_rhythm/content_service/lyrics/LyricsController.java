package com.bit_rhythm.content_service.lyrics;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.lyrics.dto.LyricsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/contents")
@RequiredArgsConstructor
public class LyricsController {

    private final LyricsService lyricsService;
    private final ResponseFactory responseFactory;

    @GetMapping("/tracks/{id}/lyrics")
    public ResponseEntity<ApiResponseFormat<Object>> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, lyricsService.get(id, CurrentUser.id(jwt)));
    }

    /** ROLE_ARTIST only (SecurityConfig: /studio/**). */
    @PutMapping("/studio/tracks/{id}/lyrics")
    public ResponseEntity<ApiResponseFormat<Object>> save(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                          @Valid @RequestBody LyricsRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, lyricsService.save(CurrentUser.id(jwt), id, request));
    }
}
