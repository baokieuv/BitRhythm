package com.bit_rhythm.notification_service.admin;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.notification_service.delivery.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ADMIN only (see SecurityConfig). Not routed by the gateway: call the service directly. */
@RestController
@RequestMapping("/api/notifications/admin/notifications/logs")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final AdminDeliveryService adminDeliveryService;
    private final ResponseFactory responseFactory;

    @GetMapping
    @PreAuthorize("hasAuthority('read:notification-logs')")
    public ResponseEntity<ApiResponseFormat<Object>> search(@RequestParam(required = false) DeliveryStatus status,
                                                            @RequestParam(required = false) Long userId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS,
                adminDeliveryService.search(status, userId, page, size));
    }

    @PostMapping("/{id}/resend")
    @PreAuthorize("hasAuthority('write:notification-logs')")
    public ResponseEntity<ApiResponseFormat<Object>> resend(@PathVariable Long id) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminDeliveryService.resend(id));
    }
}
