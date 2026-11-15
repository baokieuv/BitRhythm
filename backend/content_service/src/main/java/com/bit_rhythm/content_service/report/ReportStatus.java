package com.bit_rhythm.content_service.report;

public enum ReportStatus {
    OPEN,        // waiting for a moderator
    RESOLVED,    // acted upon (the content may have been taken down)
    DISMISSED    // no action needed
}
