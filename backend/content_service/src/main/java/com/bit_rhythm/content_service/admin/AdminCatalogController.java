package com.bit_rhythm.content_service.admin;

import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.ContentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ADMIN only (SecurityConfig). Not routed by the gateway: call the service directly. */
@RestController("/api/contents")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;
    private final ResponseFactory responseFactory;

    @GetMapping("/admin/tracks")
    @PreAuthorize("hasAuthority('read:admin:contents')")
    public ResponseEntity<ApiResponseFormat<Object>> tracks(@RequestParam(required = false) ContentStatus status,
                                                            @RequestParam(required = false) Long cursor,
                                                            @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminCatalogService.tracks(status, cursor, size));
    }

    @PostMapping("/admin/tracks/{id}/take-down")
    @PreAuthorize("hasAuthority('write:admin:contents')")
    public ResponseEntity<ApiResponseFormat<Object>> takeDownTrack(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        adminCatalogService.takeDownTrack(id, CurrentUser.id(jwt));
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PostMapping("/admin/tracks/{id}/restore")
    @PreAuthorize("hasAuthority('write:admin:contents')")
    public ResponseEntity<ApiResponseFormat<Object>> restoreTrack(@PathVariable Long id) {
        adminCatalogService.restoreTrack(id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @GetMapping("/admin/albums")
    @PreAuthorize("hasAuthority('read:admin:contents')")
    public ResponseEntity<ApiResponseFormat<Object>> albums(@RequestParam(required = false) ContentStatus status,
                                                            @RequestParam(required = false) Long cursor,
                                                            @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminCatalogService.albums(status, cursor, size));
    }

    @PostMapping("/admin/albums/{id}/take-down")
    @PreAuthorize("hasAuthority('write:admin:contents')")
    public ResponseEntity<ApiResponseFormat<Object>> takeDownAlbum(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        adminCatalogService.takeDownAlbum(id, CurrentUser.id(jwt));
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PostMapping("/admin/albums/{id}/restore")
    @PreAuthorize("hasAuthority('write:admin:contents')")
    public ResponseEntity<ApiResponseFormat<Object>> restoreAlbum(@PathVariable Long id) {
        adminCatalogService.restoreAlbum(id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
