package com.bit_rhythm.content_service.discovery;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contents/discover")
@RequiredArgsConstructor
public class DiscoveryController {

    private final DiscoveryService discoveryService;
    private final ResponseFactory responseFactory;

    @GetMapping("/popular")
    public ResponseEntity<ApiResponseFormat<Object>> popular(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, discoveryService.popular(size, CurrentUser.id(jwt)));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponseFormat<Object>> trending(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, discoveryService.trending(size, CurrentUser.id(jwt)));
    }

    @GetMapping("/for-you")
    public ResponseEntity<ApiResponseFormat<Object>> forYou(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, discoveryService.forYou(CurrentUser.id(jwt), size));
    }
}
