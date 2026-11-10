package com.bit_rhythm.user_service.avatar;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.avatar.dto.ConfirmAvatarRequest;
import com.bit_rhythm.user_service.avatar.dto.UploadUrlRequest;
import com.bit_rhythm.user_service.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;
    private final ResponseFactory responseFactory;

    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponseFormat<Object>> uploadUrl(@AuthenticationPrincipal Jwt jwt,
                                                               @Valid @RequestBody UploadUrlRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, avatarService.createUploadUrl(CurrentUser.id(jwt), request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponseFormat<Object>> confirm(@AuthenticationPrincipal Jwt jwt,
                                                             @Valid @RequestBody ConfirmAvatarRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, avatarService.confirm(CurrentUser.id(jwt), request));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseFormat<Object>> remove(@AuthenticationPrincipal Jwt jwt) {
        return responseFactory.response(CoreErrorCode.SUCCESS, avatarService.remove(CurrentUser.id(jwt)));
    }
}
