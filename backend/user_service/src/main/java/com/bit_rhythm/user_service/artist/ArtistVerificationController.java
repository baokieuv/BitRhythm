package com.bit_rhythm.user_service.artist;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.artist.dto.request.SubmitVerificationRequest;
import com.bit_rhythm.user_service.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** [S2] The user's side of "become a verified artist". */
@RestController
@RequestMapping("/api/users/me/artist-verification")
@RequiredArgsConstructor
public class ArtistVerificationController {

    private final ArtistVerificationService verificationService;
    private final ResponseFactory responseFactory;

    @PostMapping
    public ResponseEntity<ApiResponseFormat<Object>> submit(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody SubmitVerificationRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, verificationService.submit(CurrentUser.id(jwt), request.message()));
    }

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> mine(@AuthenticationPrincipal Jwt jwt) {
        return responseFactory.response(CoreErrorCode.SUCCESS, verificationService.mine(CurrentUser.id(jwt)));
    }
}
