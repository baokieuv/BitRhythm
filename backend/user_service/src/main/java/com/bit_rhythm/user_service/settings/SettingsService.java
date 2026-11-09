package com.bit_rhythm.user_service.settings;

import com.bit_rhythm.user_service.common.exception.UserCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.settings.dto.SettingsRequest;
import com.bit_rhythm.user_service.settings.dto.SettingsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final SettingsRepository settings;

    public SettingsService(SettingsRepository settings) { this.settings = settings; }

    @Transactional(readOnly = true)
    public SettingsResponse get(Long userId) {
        return SettingsResponse.from(find(userId));
    }

    @Transactional
    public SettingsResponse update(Long userId, SettingsRequest req) {
        var s = find(userId);
        s.setLocale(req.locale());
        s.setVisibility(req.visibility());
        s.setShowListeningActivity(req.showListeningActivity());
        return SettingsResponse.from(s);
    }

    private UserSettings find(Long userId) {
        return settings.findById(userId).orElseThrow(() -> new UserException(UserCode.PROFILE_NOT_FOUND));
    }
}
