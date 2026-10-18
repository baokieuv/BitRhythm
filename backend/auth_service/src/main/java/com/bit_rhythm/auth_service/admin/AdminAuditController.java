package com.bit_rhythm.auth_service.admin;

import com.bit_rhythm.auth_service.audit.AuditAction;
import com.bit_rhythm.auth_service.audit.AuditQueryService;
import com.bit_rhythm.auth_service.audit.dto.AuditLogFilter;
import com.bit_rhythm.auth_service.audit.dto.AuditLogResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasAuthority('audit:read')")
public class AdminAuditController {

    private static final int MAX_PAGE_SIZE = 200;
    // Sort is fixed server-side: newest first. The client cannot choose the sort property.
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "createdAt", "id");

    private final AuditQueryService auditQueryService;

    public AdminAuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> search(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must not be after 'to'");
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, MAX_PAGE_SIZE),
                SORT);

        AuditLogFilter filter = new AuditLogFilter(userId, actorId, action, from, to);
        return PageResponse.from(auditQueryService.search(filter, pageable));
    }
}
