package com.bit_rhythm.auth_service.admin;

import com.bit_rhythm.auth_service.admin.dto.request.ServiceClientRegisterRequest;
import com.bit_rhythm.auth_service.token.ServiceClientService;
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
@RequestMapping("/api/auth/admin/service-clients")
public class AdminServiceClientController {

    private final ServiceClientService clientService;
    private final ResponseFactory responseFactory;

    @PostMapping
    @PreAuthorize("hasAuthority('write:service-clients')")
    public ResponseEntity<ApiResponseFormat<Object>> registerClient(
            @RequestBody @Valid ServiceClientRegisterRequest request
    ) {
        return responseFactory.response(CoreErrorCode.SUCCESS, clientService.register(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('read:service-clients')")
    public ResponseEntity<ApiResponseFormat<Object>> listClients() {
        return responseFactory.response(CoreErrorCode.SUCCESS, clientService.searchServiceClients());
    }

    @PutMapping("/{clientId}/rotate-secret")
    @PreAuthorize("hasAuthority('write:service-clients')")
    public ResponseEntity<ApiResponseFormat<Object>> rotateSecret(
            @PathVariable("clientId") String clientId
    ) {
        return responseFactory.response(CoreErrorCode.SUCCESS, clientService.rotateSecret(clientId));
    }

    @DeleteMapping("/{clientId}")
    @PreAuthorize("hasAuthority('write:service-clients')")
    public ResponseEntity<ApiResponseFormat<Object>> deleteClient(
            @PathVariable("clientId") String clientId
    ) {
        clientService.deactivate(clientId);
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
