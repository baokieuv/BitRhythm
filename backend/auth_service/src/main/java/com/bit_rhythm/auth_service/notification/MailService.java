package com.bit_rhythm.auth_service.notification;

import com.bit_rhythm.auth_service.config.AppProperties;
import com.bit_rhythm.auth_service.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final AppProperties appProperties;

    public void sendVerification(User user, String rawToken,
                                 com.bit_rhythm.auth_service.verification.VerificationService.Purpose purpose) {
        String baseUrl = purpose == com.bit_rhythm.auth_service.verification.VerificationService.Purpose.PASSWORD_RESET
                ? appProperties.resetPasswordUrl() : appProperties.verifyEmailUrl();
        String link = baseUrl + "?token=" +
                java.net.URLEncoder.encode(rawToken, java.nio.charset.StandardCharsets.UTF_8);
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("Email verification link for {}: {}", user.getEmail(), link);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.mailFrom());
        message.setTo(user.getEmail());
        message.setSubject(purpose == com.bit_rhythm.auth_service.verification.VerificationService.Purpose.PASSWORD_RESET
                ? "Reset your BitRhythm password" : "Verify your BitRhythm account");
        message.setText("Use this link: " + link);
        sender.send(message);
    }
}
