package com.bit_rhythm.content_service.library;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/contents")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final ResponseFactory responseFactory;

    @PutMapping("/tracks/{id}/like")
    public ResponseEntity<ApiResponseFormat<Object>> like(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        libraryService.likeTrack(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/tracks/{id}/like")
    public ResponseEntity<ApiResponseFormat<Object>> unlike(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        libraryService.unlikeTrack(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @GetMapping("/library/tracks")
    public ResponseEntity<ApiResponseFormat<Object>> likedTracks(@AuthenticationPrincipal Jwt jwt,
                                                                 @RequestParam(required = false) Long cursor,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, libraryService.likedTracks(CurrentUser.id(jwt), cursor, size));
    }

    @PutMapping("/albums/{id}/save")
    public ResponseEntity<ApiResponseFormat<Object>> save(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        libraryService.saveAlbum(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/albums/{id}/save")
    public ResponseEntity<ApiResponseFormat<Object>> unsave(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        libraryService.unsaveAlbum(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @GetMapping("/library/albums")
    public ResponseEntity<ApiResponseFormat<Object>> savedAlbums(@AuthenticationPrincipal Jwt jwt,
                                                                 @RequestParam(required = false) Long cursor,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, libraryService.savedAlbums(CurrentUser.id(jwt), cursor, size));
    }
}
