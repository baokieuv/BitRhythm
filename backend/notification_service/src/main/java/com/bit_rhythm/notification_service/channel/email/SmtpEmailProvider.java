package com.bit_rhythm.notification_service.channel.email;

import com.bit_rhythm.notification_service.config.NotificationProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.email", name = "provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;
    private final NotificationProperties properties;

    @Override
    public String name() {
        return "smtp";
    }

    @Override
    public String send(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.email().from(), properties.email().fromName());
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.htmlBody(), true);
            mailSender.send(mime);
            return UUID.randomUUID().toString();   // SMTP gives no usable id; SES/SendGrid providers return theirs
        } catch (Exception e) {
            throw new IllegalStateException("SMTP send failed", e);   // retryable
        }
    }
}
