package com.bit_rhythm.notification_service.inbox;

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

import java.util.List;
import java.util.Map;

/** The signed-in user's inbox. User tokens only (see SecurityConfig); the user id always comes from the token. */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class InboxController {

    private final InboxService inboxService;

    @GetMapping
    public List<InboxResponse> list(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam(required = false) Long cursor,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return inboxService.list(CurrentUser.id(jwt), cursor, size, unreadOnly).stream()
                .map(InboxResponse::from)
                .toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("count", inboxService.unreadCount(CurrentUser.id(jwt)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        inboxService.markRead(CurrentUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal Jwt jwt) {
        inboxService.markAllRead(CurrentUser.id(jwt));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        inboxService.delete(CurrentUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }
}
