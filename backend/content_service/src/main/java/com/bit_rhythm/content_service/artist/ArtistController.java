package com.bit_rhythm.content_service.artist;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.album.AlbumService;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.track.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The public artist page: profile, published tracks, published albums. */
@RestController
@RequestMapping("/api/contents/artists/{id}")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final TrackService trackService;
    private final AlbumService albumService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> get(@PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, artistService.get(id));
    }

    @GetMapping("/tracks")
    public ResponseEntity<ApiResponseFormat<Object>> tracks(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                            @RequestParam(required = false) Long cursor,
                                                            @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, trackService.listByArtist(id, cursor, size, CurrentUser.id(jwt)));
    }

    @GetMapping("/albums")
    public ResponseEntity<ApiResponseFormat<Object>> albums(@PathVariable Long id,
                                                            @RequestParam(required = false) Long cursor,
                                                            @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, albumService.listByArtist(id, cursor, size));
    }
}
