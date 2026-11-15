package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.studio.dto.UploadUrlRequest;
import com.bit_rhythm.content_service.track.dto.TrackRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ARTIST only (SecurityConfig). */
@RestController
@RequestMapping("/api/contents/studio/tracks")
@RequiredArgsConstructor
public class StudioTrackController {

    private final StudioTrackService studioTrackService;
    private final UploadService uploadService;
    private final ResponseFactory responseFactory;

    @PostMapping
    public ResponseEntity<ApiResponseFormat<Object>> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TrackRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioTrackService.create(CurrentUser.id(jwt), request));
    }

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> list(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam(required = false) Long cursor,
                                                          @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioTrackService.list(CurrentUser.id(jwt), cursor, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioTrackService.get(CurrentUser.id(jwt), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                            @Valid @RequestBody TrackRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioTrackService.update(CurrentUser.id(jwt), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        studioTrackService.delete(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PostMapping("/{id}/audio/upload-url")
    public ResponseEntity<ApiResponseFormat<Object>> audioUploadUrl(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                                    @Valid @RequestBody UploadUrlRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, uploadService.createAudioUploadUrl(CurrentUser.id(jwt), id, request));
    }

    @PostMapping("/{id}/audio/confirm")
    public ResponseEntity<ApiResponseFormat<Object>> confirmAudio(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, uploadService.confirmAudio(CurrentUser.id(jwt), id));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponseFormat<Object>> publish(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioTrackService.publish(CurrentUser.id(jwt), id));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponseFormat<Object>> unpublish(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioTrackService.unpublish(CurrentUser.id(jwt), id));
    }
}
