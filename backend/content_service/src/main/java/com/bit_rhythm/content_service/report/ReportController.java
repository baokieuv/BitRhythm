package com.bit_rhythm.content_service.report;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.content_service.common.CurrentUser;
import com.bit_rhythm.content_service.report.dto.CreateReportRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contents/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ResponseFactory responseFactory;

    @PostMapping
    public ResponseEntity<ApiResponseFormat<Object>> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateReportRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, reportService.create(CurrentUser.id(jwt), request));
    }
}
