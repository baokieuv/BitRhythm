package com.bit_rhythm.notification_service.admin;

import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import com.bit_rhythm.notification_service.template.TemplateService;
import com.bit_rhythm.notification_service.template.dto.request.NotificationTemplateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/templates")
@RequiredArgsConstructor
public class AdminTemplateController {

    private final TemplateService templateService;
    private final ResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponseFormat<Object>> list() {
        return responseFactory.response(CoreErrorCode.SUCCESS, templateService.listTemplates());
    }

    @PostMapping
    public ResponseEntity<ApiResponseFormat<Object>> create(
            @RequestBody @Valid NotificationTemplateRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, templateService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> update(@PathVariable Long id,
            @RequestBody @Valid NotificationTemplateRequest request) {
        return responseFactory.response(CoreErrorCode.SUCCESS, templateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseFormat<Object>> delete(@PathVariable Long id) {
        templateService.delete(id);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
