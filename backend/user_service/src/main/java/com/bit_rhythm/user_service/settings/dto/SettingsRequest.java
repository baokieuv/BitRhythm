package com.bit_rhythm.user_service.settings.dto;

import com.bit_rhythm.user_service.settings.ProfileVisibility;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SettingsRequest(
        @NotNull @Pattern(regexp = "^(vi|en)$", message = "must be 'vi' or 'en'") String locale,
        @NotNull ProfileVisibility visibility,
        boolean showListeningActivity
) {
}
