package com.bit_rhythm.user_service.settings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<UserSettings, Long> {

    Optional<UserSettings> findByUserIdAndIsDeleted(Long userId, Integer isDeleted);  // get mine, get public settings
}
