package com.bit_rhythm.user_service.artist.dto.response;

import com.bit_rhythm.user_service.artist.ArtistVerificationRequest;
import com.bit_rhythm.user_service.artist.VerificationStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record VerificationResponse(
        Long id,
        Long userId,
        VerificationStatus status,
        String message,
        String reviewNote,
        Instant createdAt,
        Instant reviewedAt
) {
    public static VerificationResponse from(ArtistVerificationRequest request) {
        return new VerificationResponse(request.getId(), request.getUserId(), request.getStatus(), request.getMessage(),
                request.getReviewNote(), request.getCreatedAt(), request.getReviewedAt());
    }
}
