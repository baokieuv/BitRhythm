package com.bit_rhythm.notification_service.template;

import com.bit_rhythm.core.common.CommonEnums;
import com.bit_rhythm.notification_service.channel.Channel;
import com.bit_rhythm.notification_service.channel.RenderedContent;
import com.bit_rhythm.notification_service.common.exception.NotificationException;
import com.bit_rhythm.notification_service.core.NotificationRequest;
import com.bit_rhythm.notification_service.template.dto.request.NotificationTemplateRequest;
import com.bit_rhythm.notification_service.template.dto.response.NotificationTemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.]+)\\s*\\}\\}");

    private final TemplateEngine templateEngine;
    private final TemplateRepository templateRepository;
    private final MessageSource messageSource;

    public List<NotificationTemplateResponse> listTemplates() {
        List<NotificationTemplate> templates = templateRepository.findAllByIsDeleted(CommonEnums.DeletedStatus.NOT_DELETED.getCode());
        return templates.stream()
                .map(NotificationTemplateResponse::from)
                .collect(Collectors.toList());
    }

    public NotificationTemplateResponse create(NotificationTemplateRequest request) {
        NotificationTemplate template = new NotificationTemplate(request);
        templateRepository.save(template);
        return NotificationTemplateResponse.from(template);
    }

    public NotificationTemplateResponse update(Long templateId, NotificationTemplateRequest request) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NotificationException(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND",
                        "Notification template with ID " + templateId + " not found"));
        template.update(request);
        templateRepository.save(template);
        return NotificationTemplateResponse.from(template);
    }

    public void delete(Long templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NotificationException(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND",
                        "Notification template with ID " + templateId + " not found"));
        template.setIsDeleted(CommonEnums.DeletedStatus.DELETED.getCode());
        templateRepository.save(template);
    }

    public RenderedContent render(NotificationRequest request, Channel channel) {
        Locale locale = resolveLocale(request.recipient().locale());
        return switch (channel) {
            case EMAIL -> renderEmail(request, locale);
            case IN_APP -> renderInApp(request, locale);
        };
    }

    public RenderedContent renderEmail(NotificationRequest request, Locale locale) {
        String key = request.type().templateKey();
        Map<String, Object> data = request.data();

        Optional<NotificationTemplate> templateOpt = findTemplate(request, Channel.EMAIL, locale);

        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            String subject = hasText(template.getSubject())
                    ? fill(template.getSubject(), data, false)
                    : messageSource.getMessage(key + ".subject", null, locale);

            return RenderedContent.email(subject, fill(template.getBody(), data, true));
        }

        String subject = messageSource.getMessage(key + ".subject", null, locale);

        Context context = new Context(locale);
        context.setVariable("data", request.data());
        context.setVariable("recipient", request.recipient());

        String html;
        try {
            html = templateEngine.process("templates/email/" + key + "_" + locale.getLanguage(), context);
        } catch (TemplateInputException ex) {
            try {
                html = templateEngine.process("templates/email/" + key + "_" + DEFAULT_LOCALE.getLanguage(), context);
            } catch (TemplateInputException missing) {
                throw new NotificationException(HttpStatus.INTERNAL_SERVER_ERROR, "TEMPLATE_NOT_FOUND",
                        "No email template for " + key);
            }
        }
        return RenderedContent.email(subject, html);
    }

    public RenderedContent renderInApp(NotificationRequest request, Locale locale) {
        String key = request.type().templateKey();
        Map<String, Object> data = request.data();
        Object[] args = {data.getOrDefault("actorName", "")};
        Optional<NotificationTemplate> custom = findTemplate(request, Channel.IN_APP, locale);

        // In the table, "subject" is the inbox title.
        String title = textFromEvent(data, "title")
                .or(() -> custom.map(NotificationTemplate::getSubject)
                        .filter(TemplateService::hasText)
                        .map(text -> fill(text, data, false)))
                .orElseGet(() -> messageSource.getMessage(key + ".title", args, locale));

        String body = textFromEvent(data, "body")
                .or(() -> custom.map(NotificationTemplate::getBody)
                        .filter(TemplateService::hasText)
                        .map(text -> fill(text, data, false)))
                .orElseGet(() -> messageSource.getMessage(key + ".body", args, locale));

        return RenderedContent.inApp(title, body);
    }

    private Optional<NotificationTemplate> findTemplate(NotificationRequest request, Channel channel, Locale locale) {
        return templateRepository.findFirstByTypeAndChannelAndLocaleAndActiveTrueOrderByVersionDesc(
                request.type(), channel, locale.getLanguage());
    }

    private Optional<String> textFromEvent(Map<String, Object> data, String key) {
        return data.get(key) instanceof String text && !text.isBlank() ? Optional.of(text) : Optional.empty();
    }

    /** Replaces every {{name}} with data.get(name). escapeHtml = true for HTML bodies, false for plain text. */
    private String fill(String template, Map<String, Object> data, boolean escapeHtml) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            Object value = data.get(matcher.group(1));
            if (value == null) {
                throw new NotificationException(HttpStatus.UNPROCESSABLE_ENTITY, "TEMPLATE_VARIABLE_MISSING",
                        "Template variable '" + matcher.group(1) + "' has no value in the event data");
            }
            String text = value.toString();
            matcher.appendReplacement(result, Matcher.quoteReplacement(escapeHtml ? HtmlUtils.htmlEscape(text) : text));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private Locale resolveLocale(String tag) {
        return tag == null || tag.isBlank() ? DEFAULT_LOCALE : Locale.forLanguageTag(tag);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
