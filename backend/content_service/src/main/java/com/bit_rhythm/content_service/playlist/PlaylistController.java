package com.bit_rhythm.content_service.playlist;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.playlist.dto.AddTrackRequest;
import com.bit_rhythm.content_service.playlist.dto.PlaylistRequest;
import com.bit_rhythm.content_service.playlist.dto.ReorderRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/contents")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final ResponseFactory responseFactory;

    @PostMapping("/playlists")
    public ResponseEntity<ApiResponseFormat<Object>> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PlaylistRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, playlistService.create(CurrentUser.id(jwt), request));
    }

    @GetMapping("/me/playlists")
    public ResponseEntity<ApiResponseFormat<Object>> mine(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam(required = false) Long cursor,
                                                          @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, playlistService.mine(CurrentUser.id(jwt), cursor, size));
    }

    @GetMapping("/playlists/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, playlistService.get(id, CurrentUser.id(jwt)));
    }

    @PutMapping("/playlists/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                            @Valid @RequestBody PlaylistRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, playlistService.update(CurrentUser.id(jwt), id, request));
    }

    @DeleteMapping("/playlists/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        playlistService.delete(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @GetMapping("/playlists/{id}/tracks")
    public ResponseEntity<ApiResponseFormat<Object>> tracks(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                            @RequestParam(required = false) Long cursor,
                                                            @RequestParam(defaultValue = "50") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, playlistService.tracks(id, CurrentUser.id(jwt), cursor, size));
    }

    @PostMapping("/playlists/{id}/tracks")
    public ResponseEntity<ApiResponseFormat<Object>> addTrack(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                              @Valid @RequestBody AddTrackRequest request) {
        playlistService.addTrack(CurrentUser.id(jwt), id, request.trackId());
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/playlists/{id}/tracks/{trackId}")
    public ResponseEntity<ApiResponseFormat<Object>> removeTrack(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                                 @PathVariable Long trackId) {
        playlistService.removeTrack(CurrentUser.id(jwt), id, trackId);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PutMapping("/playlists/{id}/tracks/order")
    public ResponseEntity<ApiResponseFormat<Object>> reorder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                             @Valid @RequestBody ReorderRequest request) {
        playlistService.reorder(CurrentUser.id(jwt), id, request.trackIds());
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
