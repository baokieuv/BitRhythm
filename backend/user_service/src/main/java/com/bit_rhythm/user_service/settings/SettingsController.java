package com.bit_rhythm.user_service.settings;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.common.CurrentUser;
import com.bit_rhythm.user_service.settings.dto.SettingsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> get(@AuthenticationPrincipal Jwt jwt) {
        return responseFactory.response(CoreErrorCode.SUCCESS, settingsService.get(CurrentUser.id(jwt)));
    }

    @PutMapping
    public ResponseEntity<ApiResponseFormat<Object>> update(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody SettingsRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, settingsService.update(CurrentUser.id(jwt), request));
    }
}
