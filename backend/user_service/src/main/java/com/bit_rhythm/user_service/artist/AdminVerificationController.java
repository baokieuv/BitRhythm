package com.bit_rhythm.user_service.artist;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.artist.dto.request.ReviewRequest;
import com.bit_rhythm.user_service.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** [S2] ROLE_ADMIN only (SecurityConfig). */
@RestController
@RequestMapping("/api/users/admin/artist-verifications")
@RequiredArgsConstructor
public class AdminVerificationController {

    private final ArtistVerificationService verificationService;
    private final ResponseFactory responseFactory;

    @GetMapping
    @PreAuthorize("hasAuthority('read:admin:artist-verifications')")
    public ResponseEntity<ApiResponseFormat<Object>> pending(@RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, verificationService.pending(size));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('write:admin:artist-verifications')")
    public ResponseEntity<ApiResponseFormat<Object>> approve(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                             @Valid @RequestBody(required = false) ReviewRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS,
                verificationService.approve(id, CurrentUser.id(jwt), request == null ? null : request.note()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('write:admin:artist-verifications')")
    public ResponseEntity<ApiResponseFormat<Object>> reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                            @Valid @RequestBody(required = false) ReviewRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS,
                verificationService.reject(id, CurrentUser.id(jwt), request == null ? null : request.note()));
    }
}
