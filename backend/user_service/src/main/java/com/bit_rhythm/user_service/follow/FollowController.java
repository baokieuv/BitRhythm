package com.bit_rhythm.user_service.follow;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{id}")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final ResponseFactory responseFactory;

    @PutMapping("/follow")
    public ResponseEntity<ApiResponseFormat<Object>> follow(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        followService.follow(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/follow")
    public ResponseEntity<ApiResponseFormat<Object>> unfollow(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        followService.unfollow(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @GetMapping("/followers")
    public ResponseEntity<ApiResponseFormat<Object>> followers(@PathVariable Long id,
                                                               @RequestParam(required = false) Long cursor,
                                                               @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, followService.followers(id, cursor, size));
    }

    @GetMapping("/following")
    public ResponseEntity<ApiResponseFormat<Object>> following(@PathVariable Long id,
                                                               @RequestParam(required = false) Long cursor,
                                                               @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, followService.following(id, cursor, size));
    }
}
