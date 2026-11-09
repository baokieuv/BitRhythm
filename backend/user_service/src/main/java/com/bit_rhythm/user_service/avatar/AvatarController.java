package com.bit_rhythm.user_service.avatar;

import com.bit_rhythm.user_service.avatar.dto.*;
import com.bit_rhythm.user_service.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users/me/avatar")
public class AvatarController {

    private final AvatarService service;

    public AvatarController(AvatarService service) { this.service = service; }

    @PostMapping("/upload-url")
    public UploadUrlResponse uploadUrl(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UploadUrlRequest req) {
        return service.requestUpload(CurrentUser.id(jwt), req);
    }

    @PostMapping("/confirm")
    public Map<String, String> confirm(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConfirmAvatarRequest req) {
        return Map.of("avatarUrl", service.confirm(CurrentUser.id(jwt), req));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt) {
        service.remove(CurrentUser.id(jwt));
        return ResponseEntity.noContent().build();
    }
}
