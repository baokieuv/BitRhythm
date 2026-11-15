package com.bit_rhythm.user_service.artist;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.user_service.artist.dto.response.VerificationResponse;
import com.bit_rhythm.user_service.common.exception.UserErrorCode;
import com.bit_rhythm.user_service.common.exception.UserException;
import com.bit_rhythm.user_service.event.UserEventPublisher;
import com.bit_rhythm.user_service.profile.ProfileRepository;
import com.bit_rhythm.user_service.profile.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistVerificationService {

    private final VerificationRepository verificationRepository;
    private final ProfileRepository profileRepository;
    private final UserEventPublisher eventPublisher;
    private final AuthClient authClient;
    private final TransactionTemplate transactionTemplate;

    /** Submit a new verification request for the user. */
    public VerificationResponse submit(Long userId, String message) {

        if (profileRepository.existsByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new UserException(UserErrorCode.PROFILE_NOT_FOUND);
        }

        if (verificationRepository.existsByUserIdAndStatus(userId, VerificationStatus.PENDING)) {
            throw new UserException(UserErrorCode.VERIFICATION_ALREADY_PENDING);
        }

        if (verificationRepository.existsByUserIdAndStatus(userId, VerificationStatus.APPROVED)) {
            throw new UserException(UserErrorCode.ALREADY_VERIFIED);
        }

        ArtistVerificationRequest request = new ArtistVerificationRequest(userId, message);

        return VerificationResponse.from(verificationRepository.save(request));
    }

    /** Get the latest verification request for the user. */
    public VerificationResponse mine(Long userId) {

        if (profileRepository.existsByUserIdAndIsDeleted(userId, CommonEnums.DeletedStatus.NOT_DELETED.getCode())) {
            throw new UserException(UserErrorCode.PROFILE_NOT_FOUND);
        }

        ArtistVerificationRequest request = verificationRepository.findFirstByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.VERIFICATION_NOT_FOUND));

        return VerificationResponse.from(request);
    }

    /** Get all pending verification requests. */
    public List<VerificationResponse> pending(int size) {
        List<ArtistVerificationRequest> request = verificationRepository
                .findByStatusOrderByIdAsc(VerificationStatus.PENDING, Pageable.ofSize(size));

        return request.stream().map(VerificationResponse::from).toList();
    }

    /** Approve a verification request. */
    public VerificationResponse approve(Long requestId, Long adminId, String note) {

        ArtistVerificationRequest request = verificationRepository.findById(requestId)
                .orElseThrow(() -> new UserException(UserErrorCode.VERIFICATION_NOT_FOUND));

        if (request.getStatus() == VerificationStatus.APPROVED) {
            return VerificationResponse.from(request);
        }

        transactionTemplate.executeWithoutResult(status -> {
            request.setStatus(VerificationStatus.APPROVED);
            request.setReviewedBy(adminId);
            request.setReviewedAt(Instant.now());
            request.setReviewNote(note);
            verificationRepository.save(request);

            UserProfile profile = profileRepository.findByUserIdAndIsDeleted(request.getUserId(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                    .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));
            profile.setVerified(true);
            profileRepository.save(profile);

            authClient.grantRoles(profile.getUserId(), List.of("ARTIST"));

            eventPublisher.publishArtistVerificationResult(profile, request.getStatus().name(), request.getReviewNote());
        });

        return VerificationResponse.from(request);
    }

    /** Reject a verification request. */
    public VerificationResponse reject(Long requestId, Long adminId, String note) {

        ArtistVerificationRequest request = verificationRepository.findById(requestId)
                .orElseThrow(() -> new UserException(UserErrorCode.VERIFICATION_NOT_FOUND));

        UserProfile profile = profileRepository.findByUserIdAndIsDeleted(request.getUserId(), CommonEnums.DeletedStatus.NOT_DELETED.getCode())
                .orElseThrow(() -> new UserException(UserErrorCode.PROFILE_NOT_FOUND));

        request.setStatus(VerificationStatus.REJECTED);
        request.setReviewedBy(adminId);
        request.setReviewedAt(Instant.now());
        request.setReviewNote(note);

        transactionTemplate.executeWithoutResult(status -> {
            verificationRepository.save(request);

            eventPublisher.publishArtistVerificationResult(profile, request.getStatus().name(), request.getReviewNote());
        });

        return VerificationResponse.from(request);
    }
}
