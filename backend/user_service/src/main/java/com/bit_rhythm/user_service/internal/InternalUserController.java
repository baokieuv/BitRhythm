package com.bit_rhythm.user_service.internal;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [S2, optional] For other services (e.g. content-service showing the owner of a playlist). Service tokens only: the caller
 * needs scope users:read and "user-service" in its allowed audiences. Hint: cap the number of ids (e.g. 100).
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final ProfileService profileService;
    private final ResponseFactory responseFactory;

    @GetMapping("/summaries")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<ApiResponseFormat<Object>> summaries(@RequestParam List<Long> ids) {
        return responseFactory.response(CoreErrorCode.SUCCESS, profileService.summaries(ids));
    }
}
