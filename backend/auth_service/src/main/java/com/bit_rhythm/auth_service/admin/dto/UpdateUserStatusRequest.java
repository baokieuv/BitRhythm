package com.bit_rhythm.auth_service.admin.dto;

import com.bit_rhythm.auth_service.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {
}
