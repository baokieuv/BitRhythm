package com.bit_rhythm.notification_service.template;

import com.bit_rhythm.core.entity.BaseEntity;
import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.common.exception.NotificationException;
import com.bit_rhythm.notification_service.core.NotificationType;
import com.bit_rhythm.notification_service.template.dto.request.NotificationTemplateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Locale;

/** A template edited through the admin API. When none is active, the file in resources/templates is used. */
@Entity
@Table(name = "notification_templates")
@Getter
@Setter
@NoArgsConstructor
public class NotificationTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false)
    private int version;

    private String subject;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String body;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    /** New template (version 1 and active unless the request says otherwise). */
    public NotificationTemplate(NotificationTemplateRequest request) {
        this.type = request.type();
        this.channel = request.channel();
        this.locale = normalizeLocale(request.locale());
        this.version = request.version() == null ? 1 : request.version();
        this.subject = blankToNull(request.subject());
        this.body = request.body();
        this.active = request.active() == null || request.active();
    }

    /**
     * Changes what is sent: subject, body and (when given) active. type, channel, locale and version identify the
     * template, so a request that tries to change them is refused: create another template / version instead.
     */
    public void update(NotificationTemplateRequest request) {
        boolean sameIdentity = request.type() == type
                && request.channel() == channel
                && normalizeLocale(request.locale()).equals(locale)
                && (request.version() == null || request.version() == version);
        if (!sameIdentity) {
            throw new NotificationException(HttpStatus.BAD_REQUEST, "TEMPLATE_IDENTITY_CHANGED",
                    "type, channel, locale and version cannot be changed; create a new template instead");
        }
        this.subject = blankToNull(request.subject());
        this.body = request.body();
        if (request.active() != null) {
            this.active = request.active();
        }
    }

    /** "vi-VN", "VI" -> "vi": TemplateService looks templates up by the language of the recipient's locale. */
    private static String normalizeLocale(String locale) {
        return Locale.forLanguageTag(locale.trim().replace('_', '-')).getLanguage();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}