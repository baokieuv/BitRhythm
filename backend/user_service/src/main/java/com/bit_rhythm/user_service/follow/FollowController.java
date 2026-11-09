package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.user_service.common.CurrentUser;
import com.bit_rhythm.user_service.common.CursorPage;
import com.bit_rhythm.user_service.follow.dto.FollowUserResponse;
import com.bit_rhythm.user_service.profile.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{id}")
public class FollowController {

    private final FollowService service;
    private final ProfileService profileService;

    public FollowController(FollowService service, ProfileService profileService) {
        this.service = service;
        this.profileService = profileService;
    }

    @PutMapping("/follow")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        profileService.getOrCreate(jwt);                 // đảm bảo người follow đã có hồ sơ
        service.follow(CurrentUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/follow")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.unfollow(CurrentUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followers")
    public CursorPage<FollowUserResponse> followers(@PathVariable Long id,
                                                    @RequestParam(required = false) Long cursor,
                                                    @RequestParam(defaultValue = "20") int size) {
        return service.followers(id, cursor, size);
    }

    @GetMapping("/following")
    public CursorPage<FollowUserResponse> following(@PathVariable Long id,
                                                    @RequestParam(required = false) Long cursor,
                                                    @RequestParam(defaultValue = "20") int size) {
        return service.following(id, cursor, size);
    }
}
