package com.bit_rhythm.notification_service.channel;

/** EMAIL: subject + HTML body. IN_APP: title + plain body. */
public record RenderedContent(String subject, String title, String body) {

    public static RenderedContent email(String subject, String htmlBody) {
        return new RenderedContent(subject, null, htmlBody);
    }

    public static RenderedContent inApp(String title, String body) {
        return new RenderedContent(null, title, body);
    }
}
