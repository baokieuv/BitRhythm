package com.bit_rhythm.content_service.artist;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.artist.dto.UpdateArtistRequest;
import com.bit_rhythm.content_service.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ARTIST only (SecurityConfig): the artist edits their own page. */
@RestController
@RequestMapping("/api/contents/studio/artist")
@RequiredArgsConstructor
public class StudioArtistController {

    private final ArtistService artistService;
    private final ResponseFactory responseFactory;

    @GetMapping
    @PreAuthorize("hasAuthority('read:studio:artist')")
    public ResponseEntity<ApiResponseFormat<Object>> mine(@AuthenticationPrincipal Jwt jwt) {
        return responseFactory.response(CoreErrorCode.SUCCESS, artistService.mine(CurrentUser.id(jwt)));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('write:studio:artist')")
    public ResponseEntity<ApiResponseFormat<Object>> save(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody UpdateArtistRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, artistService.saveMine(CurrentUser.id(jwt), request));
    }
}
