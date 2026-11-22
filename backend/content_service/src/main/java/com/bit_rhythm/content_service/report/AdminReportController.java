package com.bit_rhythm.content_service.report;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.report.dto.ResolveReportRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ROLE_ADMIN only (SecurityConfig). Not routed by the gateway: call the service directly. */
@RestController
@RequestMapping("/api/contents/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final ResponseFactory responseFactory;

    @GetMapping
    @PreAuthorize("hasAuthority('read:admin:reports')")
    public ResponseEntity<ApiResponseFormat<Object>> queue(@RequestParam(defaultValue = "OPEN") ReportStatus status,
                                                           @RequestParam(required = false) Long cursor,
                                                           @RequestParam(defaultValue = "20") int size) {
        return responseFactory.response(CoreErrorCode.SUCCESS, reportService.queue(status, cursor, size));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('write:admin:reports')")
    public ResponseEntity<ApiResponseFormat<Object>> resolve(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                             @Valid @RequestBody ResolveReportRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS,
                reportService.resolve(id, CurrentUser.id(jwt), request.note(), request.takeDown()));
    }

    @PostMapping("/{id}/dismiss")
    @PreAuthorize("hasAuthority('write:admin:reports')")
    public ResponseEntity<ApiResponseFormat<Object>> dismiss(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                                             @RequestParam(required = false) String note) {
        return responseFactory.response(CoreErrorCode.SUCCESS, reportService.dismiss(id, CurrentUser.id(jwt), note));
    }
}
