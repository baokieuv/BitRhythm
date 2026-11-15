package com.bit_rhythm.auth_service.internal;

import com.bit_rhythm.auth_service.rbac.RbacService;
import com.bit_rhythm.auth_service.rbac.dto.request.AssignRolesRequest;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class InternalRbacController {

    private final ResponseFactory responseFactory;
    private final RbacService rbacService;

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('write:internal:role_assignments')")
    public ResponseEntity<ApiResponseFormat<Object>> grantRole(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolesRequest request
    ) {
        rbacService.assignRoles(id, request);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
