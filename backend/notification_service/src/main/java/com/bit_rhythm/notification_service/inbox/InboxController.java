package com.bit_rhythm.notification_service.inbox;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.notification_service.common.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** The signed-in user's inbox. User tokens only (see SecurityConfig); the user id always comes from the token. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class InboxController {

    private final InboxService inboxService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> list(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam(required = false) Long cursor,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return responseFactory.response(CoreErrorCode.SUCCESS, inboxService.list(CurrentUser.id(jwt), cursor, size, unreadOnly));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponseFormat<Object>> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return responseFactory.response(CoreErrorCode.SUCCESS, Map.of("count", inboxService.unreadCount(CurrentUser.id(jwt))));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponseFormat<Object>> markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        inboxService.markRead(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponseFormat<Object>> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        inboxService.markAllRead(CurrentUser.id(jwt));
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        inboxService.delete(CurrentUser.id(jwt), id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
