package com.bit_rhythm.auth_service.verification;

import com.bit_rhythm.auth_service.verification.dto.request.ResendVerificationRequest;
import com.bit_rhythm.auth_service.verification.dto.request.VerificationRequest;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.exception.CoreErrorCode;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final ResponseFactory responseFactory;

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponseFormat<Object>> verifyEmail(
            @Valid @RequestBody VerificationRequest request) {
        verificationService.verifyEmail(request.token());
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponseFormat<Object>> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationRequest request) {
        verificationService.resendVerificationEmail(request.email());
        return responseFactory.response(CoreErrorCode.SUCCESS, null);
    }
}
