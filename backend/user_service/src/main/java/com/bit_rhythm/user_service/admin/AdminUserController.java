package com.bit_rhythm.user_service.admin;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.user_service.profile.ProfileStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ADMIN only (SecurityConfig). Not routed by the gateway: call the service directly. */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> search(@RequestParam(required = false) ProfileStatus status,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, adminUserService.search(status, page, size));
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<ApiResponseFormat<Object>> hide(@PathVariable Long id) {
        adminUserService.hide(id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PostMapping("/{id}/unhide")
    public ResponseEntity<ApiResponseFormat<Object>> unhide(@PathVariable Long id) {
        adminUserService.unhide(id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
