package com.bit_rhythm.user_service.profile;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.common.CurrentUser;
import com.bit_rhythm.user_service.profile.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ResponseFactory responseFactory;

    @GetMapping("/me")
    public ResponseEntity<ApiResponseFormat<Object>> me(@AuthenticationPrincipal Jwt jwt) {
        return responseFactory.response(CoreErrorCode.SUCCESS, profileService.getMine(jwt));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponseFormat<Object>> updateMe(@AuthenticationPrincipal Jwt jwt,
                                                              @Valid @RequestBody UpdateProfileRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, profileService.updateMine(jwt, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> byId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, profileService.getPublic(id, CurrentUser.id(jwt)));
    }
}
