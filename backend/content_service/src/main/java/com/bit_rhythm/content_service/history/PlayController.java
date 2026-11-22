package com.bit_rhythm.content_service.history;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.history.dto.PlayRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class PlayController {

    private final PlayService playService;
    private final ResponseFactory responseFactory;

    @PostMapping("/tracks/{id}/play")
    public ResponseEntity<ApiResponseFormat<Object>> play(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                          @Valid @RequestBody PlayRequest request) {
        playService.recordPlay(CurrentUser.id(jwt), id, request.positionSeconds());
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @GetMapping("/library/history")
    public ResponseEntity<ApiResponseFormat<Object>> history(@AuthenticationPrincipal Jwt jwt,
                                                             @RequestParam(required = false) Long cursor,
                                                             @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, playService.history(CurrentUser.id(jwt), cursor, size));
    }
}