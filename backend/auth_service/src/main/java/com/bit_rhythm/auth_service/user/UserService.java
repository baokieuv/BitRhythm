package com.bit_rhythm.auth_service.user;

import com.bit_rhythm.auth_service.auth.dto.request.RegisterRequest;
import com.bit_rhythm.auth_service.common.enums.UserStatus;
import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.common.exception.AuthException;
import com.bit_rhythm.auth_service.rbac.Role;
import com.bit_rhythm.auth_service.rbac.RoleRepository;
import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.core.exception.ApiException;
import com.bit_rhythm.core.exception.CoreErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Transactional
    public User register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthException(AuthCode.USER_ALREADY_EXISTS);
        }

        HashSet<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByNameIgnoreCaseAndIsDeleted(
                "USER",
                CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        if (userRole != null) {
            roles.add(userRole);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.PENDING_VERIFICATION.name())
                .emailVerified(false)
                .mfaEnabled(false)
                .failedLoginAttempts(0)
                .version(0L)
                .passwordChangedAt(LocalDateTime.now())
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findOptionalByIdentifier(String identifier) {
        String normalized = identifier.trim();
        Optional<User> user = normalized.contains("@")
                ? userRepository.findByEmailIgnoreCase(normalized)
                : userRepository.findByUsernameIgnoreCase(normalized);
        return user.filter(value -> value.getIsDeleted().equals(CommonEnums.DeletedStatus.NOT_DELETED.getCode()))
                ;
    }

    @Transactional(readOnly = true)
    public Optional<User> findOptionalByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(value -> value.getIsDeleted().equals(CommonEnums.DeletedStatus.NOT_DELETED.getCode()));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .filter(value -> value.getIsDeleted().equals(CommonEnums.DeletedStatus.NOT_DELETED.getCode()))
                .orElseThrow(() -> new ApiException(CoreErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
