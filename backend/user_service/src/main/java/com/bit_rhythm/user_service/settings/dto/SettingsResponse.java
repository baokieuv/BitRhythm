package com.bit_rhythm.user_service.settings.dto;

import com.bit_rhythm.user_service.settings.ProfileVisibility;
import com.bit_rhythm.user_service.settings.UserSettings;

public record SettingsResponse(String locale, ProfileVisibility visibility, boolean showListeningActivity) {
    public static SettingsResponse from(UserSettings s) {
        return new SettingsResponse(s.getLocale(), s.getVisibility(), s.isShowListeningActivity());
    }
}
