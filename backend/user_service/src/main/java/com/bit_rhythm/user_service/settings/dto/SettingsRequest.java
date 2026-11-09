package com.bit_rhythm.user_service.settings.dto;

import com.bit_rhythm.user_service.settings.ProfileVisibility;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SettingsRequest(
        @NotNull @Pattern(regexp = "vi|en") String locale,
        @NotNull ProfileVisibility visibility,
        @NotNull Boolean showListeningActivity) {}
