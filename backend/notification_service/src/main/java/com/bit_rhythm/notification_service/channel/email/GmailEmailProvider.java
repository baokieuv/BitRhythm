package com.bit_rhythm.notification_service.channel.email;

import com.bit_rhythm.notification_service.common.exception.NotificationException;
import com.bit_rhythm.notification_service.config.NotificationProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

/**
 * Sample provider: Gmail over SMTP (smtp.gmail.com:587, STARTTLS) with a Google App Password.
 * Select it with notification.email.provider=gmail.
 *
 * Gmail specifics this class deals with:
 *  - the From address is always the authenticated account (Gmail rewrites any other one);
 *  - the account password does not work, it needs an App Password (2-Step Verification must be on).
 *
 * Nothing here limits how many emails are sent: Gmail has its own daily cap (about 500 recipients for a free
 * account) and may block the account when it is exceeded. Fine for development, demos and low volume. For real
 * traffic add a transactional provider (SES, SendGrid, ...) as another EmailProvider implementation.
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "notification.email", name = "provider", havingValue = "gmail")
public class GmailEmailProvider implements EmailProvider {
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;

    private final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();   // not a bean: independent of spring.mail.*
    private final String username;
    private final String fromName;

    public GmailEmailProvider(NotificationProperties properties) {
        NotificationProperties.Gmail gmail = properties.email().gmail();
        if (gmail == null || isBlank(gmail.username()) || isBlank(gmail.appPassword())) {
            throw new IllegalStateException(
                    "notification.email.provider=gmail needs GMAIL_USERNAME and GMAIL_APP_PASSWORD "
                            + "(a Google App Password, not the account password)");
        }
        this.username = gmail.username().trim();
        this.fromName = properties.email().fromName();

        mailSender.setHost(HOST);
        mailSender.setPort(PORT);
        mailSender.setUsername(username);
        // Google displays App Passwords as "abcd efgh ijkl mnop": the spaces are not part of the secret
        mailSender.setPassword(gmail.appPassword().replace(" ", ""));
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties smtp = mailSender.getJavaMailProperties();
        smtp.put("mail.transport.protocol", "smtp");
        smtp.put("mail.smtp.auth", "true");
        smtp.put("mail.smtp.starttls.enable", "true");
        smtp.put("mail.smtp.starttls.required", "true");
        smtp.put("mail.smtp.connectiontimeout", "5000");
        smtp.put("mail.smtp.timeout", "10000");
        smtp.put("mail.smtp.writetimeout", "10000");
    }


    @Override
    public String name() {
        return "gmail";
    }

    @Override
    public String send(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, StandardCharsets.UTF_8.name());
            helper.setFrom(username, fromName);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.htmlBody(), true);
            mailSender.send(mime);

            String messageId = mime.getMessageID();

            log.info("Gmail email sent with message ID: {}", messageId);

            return messageId != null ? messageId : UUID.randomUUID().toString();

        } catch (MailAuthenticationException e) {
            // wrong App Password / 2-Step Verification off: retrying cannot fix it
            throw new NotificationException(HttpStatus.BAD_GATEWAY, "GMAIL_AUTH_FAILED",
                    "Gmail rejected the credentials: check GMAIL_USERNAME and GMAIL_APP_PASSWORD");
        } catch (Exception e) {
            throw new IllegalStateException("Gmail send failed", e);   // network / transient: retryable
        }

    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
