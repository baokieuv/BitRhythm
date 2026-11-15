package com.bit_rhythm.content_service.common.exception;

import com.bit_rhythm.core.exception.ApiException;

/**
 * Business error of content-service. The HTTP response comes from the ContentErrorCode through core's GlobalExceptionHandler.
 * The optional detail only goes to the logs (getMessage), not to the client.
 */
public class ContentException extends ApiException {

    private final String detail;

    public ContentException(ContentErrorCode code) {
        this(code, null);
    }

    public ContentException(ContentErrorCode code, String detail) {
        super(code);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String getMessage() {
        String base = getErrorCode().getMessage();
        return detail == null ? base : base + ": " + detail;
    }
}
