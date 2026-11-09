package com.bit_rhythm.user_service.profile;

import com.bit_rhythm.user_service.common.CurrentUser;
import com.bit_rhythm.user_service.profile.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) { this.service = service; }

    @GetMapping("/me")
    public ProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
        return service.getMine(jwt);
    }

    @PutMapping("/me")
    public ProfileResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest req) {
        return service.updateMine(jwt, req);
    }

    @GetMapping("/{id}")
    public PublicProfileResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.getPublic(id, CurrentUser.id(jwt));
    }
}
