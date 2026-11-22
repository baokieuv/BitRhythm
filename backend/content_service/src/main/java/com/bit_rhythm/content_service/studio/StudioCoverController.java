package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.studio.dto.UploadUrlRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ARTIST only (SecurityConfig). One endpoint for the covers of tracks, albums and the artist page. */
@RestController
@RequestMapping("/api/contents/studio/covers")
@RequiredArgsConstructor
public class StudioCoverController {

    private final UploadService uploadService;
    private final ResponseFactory responseFactory;

    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponseFormat<Object>> uploadUrl(@AuthenticationPrincipal Jwt jwt,
                                                               @Valid @RequestBody UploadUrlRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, uploadService.createCoverUploadUrl(CurrentUser.id(jwt), request));
    }
}
