package com.bit_rhythm.user_service.block;

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
@RequestMapping("/users")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;
    private final ResponseFactory responseFactory;

    @PutMapping("/{id}/block")
    public ResponseEntity<ApiResponseFormat<Object>> block(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        blockService.block(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/{id}/block")
    public ResponseEntity<ApiResponseFormat<Object>> unblock(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        blockService.unblock(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @GetMapping("/me/blocks")
    public ResponseEntity<ApiResponseFormat<Object>> blocked(@AuthenticationPrincipal Jwt jwt,
                                                             @RequestParam(required = false) Long cursor,
                                                             @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, blockService.blocked(CurrentUser.id(jwt), cursor, size));
    }
}
