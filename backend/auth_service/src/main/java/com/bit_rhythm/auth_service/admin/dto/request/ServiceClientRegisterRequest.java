package com.bit_rhythm.auth_service.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ServiceClientRegisterRequest(
        @NotBlank String clientId,
        @NotNull List<String> scopes,
        @NotNull List<String> audiences
) {
}
