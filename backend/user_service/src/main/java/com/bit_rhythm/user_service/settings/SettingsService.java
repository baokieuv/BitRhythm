package com.bit_rhythm.user_service.settings;

import com.bit_rhythm.user_service.settings.dto.SettingsRequest;
import com.bit_rhythm.user_service.settings.dto.SettingsResponse;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    /** [S1] TODO(hint): the row is created together with the profile (ProfileService.getOrCreate); if it is missing, create defaults. */
    public SettingsResponse get(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): replace locale, visibility and showListeningActivity; save; map. */
    public SettingsResponse update(Long userId, SettingsRequest request) {
        throw new UnsupportedOperationException("TODO");
    }

    /** [S1] TODO(hint): used by UserEventPublisher to put the RECIPIENT's locale into notifications; default "en". */
    public String localeOf(Long userId) {
        throw new UnsupportedOperationException("TODO");
    }
}
