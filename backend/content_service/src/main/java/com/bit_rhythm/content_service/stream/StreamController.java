package com.bit_rhythm.content_service.stream;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contents/tracks/{id}/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> stream(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, streamService.streamUrl(id, CurrentUser.id(jwt)));
    }
}
