package com.bit_rhythm.user_service.settings;

import com.bit_rhythm.user_service.common.CurrentUser;
import com.bit_rhythm.user_service.profile.ProfileService;
import com.bit_rhythm.user_service.settings.dto.SettingsRequest;
import com.bit_rhythm.user_service.settings.dto.SettingsResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/settings")
public class SettingsController {

    private final SettingsService service;
    private final ProfileService profileService;

    public SettingsController(SettingsService service, ProfileService profileService) {
        this.service = service;
        this.profileService = profileService;
    }

    @GetMapping
    public SettingsResponse get(@AuthenticationPrincipal Jwt jwt) {
        profileService.getOrCreate(jwt);
        return service.get(CurrentUser.id(jwt));
    }

    @PutMapping
    public SettingsResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SettingsRequest req) {
        profileService.getOrCreate(jwt);
        return service.update(CurrentUser.id(jwt), req);
    }
}
