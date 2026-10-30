package com.bit_rhythm.auth_service.auth;

import com.bit_rhythm.auth_service.user.User;
import com.bit_rhythm.auth_service.user.UserRepository;
import com.bit_rhythm.auth_service.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;

    public boolean isLocked(User user) {
        LocalDateTime lockedUntil = user.getLockedUntil();
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    @Transactional
    public void recordFailure(User user) {
        int failures = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        failures++;
        user.setFailedLoginAttempts(failures);
        if (failures >= MAX_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plus(LOCK_DURATION));
            user.setStatus(UserStatus.LOCKED.name());
        }
        userRepository.save(user);
    }

    @Transactional
    public void recordSuccess(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setStatus(UserStatus.ACTIVE.name());
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
