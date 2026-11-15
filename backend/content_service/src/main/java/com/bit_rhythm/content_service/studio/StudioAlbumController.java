package com.bit_rhythm.content_service.studio;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.album.dto.AlbumRequest;
import com.bit_rhythm.content_service.common.CurrentUser;
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
@RequestMapping("/api/contents/studio/albums")
@RequiredArgsConstructor
public class StudioAlbumController {

    private final StudioAlbumService studioAlbumService;
    private final ResponseFactory responseFactory;

    @PostMapping
    public ResponseEntity<ApiResponseFormat<Object>> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AlbumRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioAlbumService.create(CurrentUser.id(jwt), request));
    }

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> list(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam(required = false) Long cursor,
                                                          @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioAlbumService.list(CurrentUser.id(jwt), cursor, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                            @Valid @RequestBody AlbumRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioAlbumService.update(CurrentUser.id(jwt), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        studioAlbumService.delete(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponseFormat<Object>> publish(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioAlbumService.publish(CurrentUser.id(jwt), id));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponseFormat<Object>> unpublish(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, studioAlbumService.unpublish(CurrentUser.id(jwt), id));
    }
}
