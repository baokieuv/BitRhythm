package com.bit_rhythm.user_service.settings;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.settings.dto.SettingsRequest;
import com.bit_rhythm.user_service.settings.dto.SettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsResponse get(Long userId) {

        UserSettings settings = findActive(userId)
                .orElseGet(() -> {
                    UserSettings defaultSettings = new UserSettings();
                    defaultSettings.setUserId(userId);
                    defaultSettings.setLocale("en");
                    defaultSettings.setVisibility(ProfileVisibility.PUBLIC);
                    defaultSettings.setShowListeningActivity(true);
                    return settingsRepository.save(defaultSettings);
                });

        return SettingsResponse.from(settings);
    }

    public SettingsResponse update(Long userId, SettingsRequest request) {
        UserSettings settings = findActive(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        settings.update(request);
        UserSettings saved = settingsRepository.save(settings);

        return SettingsResponse.from(saved);
    }

    /** used by UserEventPublisher to put the RECIPIENT's locale into notifications; default "en". */
    public String localeOf(Long userId) {
        UserSettings settings = findActive(userId)
                .orElse(null);

        if (settings == null) {
            return "en";  // default locale
        }

        return settings.getLocale();
    }

    private Optional<UserSettings> findActive(Long userId) {
        return settingsRepository.findByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode());
    }
}
