package com.bit_rhythm.auth_service.verification;

import com.bit_rhythm.auth_service.common.exception.AuthCode;
import com.bit_rhythm.auth_service.verification.dto.request.ResendVerificationRequest;
import com.bit_rhythm.auth_service.verification.dto.request.VerificationRequest;
import com.bit_rhythm.core.dto.ApiResponseFormat;
import com.bit_rhythm.core.utils.ResponseFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;
    private final ResponseFactory responseFactory;

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponseFormat<Object>> verify(
            @Valid @RequestBody VerificationRequest request) {
        verificationService.verify(request.token());
        return responseFactory.response(AuthCode.SUCCESS, null);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponseFormat<Object>> resend(
            @Valid @RequestBody ResendVerificationRequest request) {
        verificationService.resend(request.email());
        return responseFactory.response(AuthCode.SUCCESS, null);
    }
}
