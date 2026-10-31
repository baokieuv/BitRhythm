package com.bit_rhythm.gateway.filter;

import java.io.IOException;

public class RequestTooLargeException extends IOException {
    public RequestTooLargeException() {
        super("Request body exceeds the configured limit");
    }
}
