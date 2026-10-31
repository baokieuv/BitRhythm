package com.bit_rhythm.gateway.filter;

import com.bit_rhythm.gateway.config.GatewayProperties;
import com.bit_rhythm.gateway.error.Causes;
import com.bit_rhythm.gateway.error.ProblemResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Rejects oversized bodies, including chunked ones without Content-Length: bytes are counted while
 * the proxy streams the body upstream. Runs inside CorrelationIdFilter and AccessLogFilter so a 413
 * still carries a correlation id and shows up in the access log.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RequestSizeFilter extends OncePerRequestFilter {

    private final GatewayProperties properties;

    public RequestSizeFilter(GatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long max = properties.maxRequestBodyBytes();
        if (request.getContentLengthLong() > max) {
            reject(request, response);
            return;
        }
        try {
            filterChain.doFilter(new SizeLimitedRequest(request, max), response);
        } catch (IOException | ServletException | RuntimeException exception) {
            // The proxy may wrap our exception (ServletException, ResourceAccessException...).
            if (Causes.has(exception, RequestTooLargeException.class) && !response.isCommitted()) {
                reject(request, response);
            } else {
                throw exception;
            }
        }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProblemResponses.write(request, response, HttpStatus.valueOf(413),
                "Request body exceeds the configured limit");
    }

    private static final class SizeLimitedRequest extends HttpServletRequestWrapper {
        private final long maxBytes;

        private SizeLimitedRequest(HttpServletRequest request, long maxBytes) {
            super(request);
            this.maxBytes = maxBytes;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            ServletInputStream delegate = super.getInputStream();
            return new ServletInputStream() {
                private long bytesRead;

                @Override
                public int read() throws IOException {
                    int value = delegate.read();
                    if (value >= 0) {
                        count(1);
                    }
                    return value;
                }

                @Override
                public int read(byte[] bytes, int offset, int length) throws IOException {
                    int count = delegate.read(bytes, offset, length);
                    if (count > 0) {
                        count(count);
                    }
                    return count;
                }

                private void count(int amount) throws RequestTooLargeException {
                    bytesRead += amount;
                    if (bytesRead > maxBytes) {
                        throw new RequestTooLargeException();
                    }
                }

                @Override
                public boolean isFinished() {
                    return delegate.isFinished();
                }

                @Override
                public boolean isReady() {
                    return delegate.isReady();
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    delegate.setReadListener(listener);
                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null ? Charset.defaultCharset() : Charset.forName(encoding);
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }
    }
}
