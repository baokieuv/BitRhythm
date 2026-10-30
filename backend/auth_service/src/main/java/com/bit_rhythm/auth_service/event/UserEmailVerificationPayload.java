package com.bit_rhythm.auth_service.event;

public record UserEmailVerificationPayload(
        String username,
        String email,
        String verificationCode
) {
}
