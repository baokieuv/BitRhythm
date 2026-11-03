package com.bit_rhythm.core.serviceauth;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Adds "Authorization: Bearer <service token>" to every outgoing call and retries once on 401
 * with a fresh token. Usage:
 *
 * <pre>
 * RestClient ordersClient = RestClient.builder()
 *         .baseUrl("http://orders-service")
 *         .requestInterceptor(new ServiceAuthInterceptor(serviceTokenProvider, "orders-service"))
 *         .build();
 * </pre>
 */
public class ServiceAuthInterceptor implements ClientHttpRequestInterceptor {

    private final ServiceTokenProvider tokenProvider;
    private final String audience;

    public ServiceAuthInterceptor(ServiceTokenProvider tokenProvider, String audience) {
        this.tokenProvider = tokenProvider;
        this.audience = audience;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        request.getHeaders().setBearerAuth(tokenProvider.getAccessToken(audience));
        ClientHttpResponse response = execution.execute(request, body);

        if (response.getStatusCode().value() == 401) {
            response.close();
            tokenProvider.invalidate(audience);
            request.getHeaders().setBearerAuth(tokenProvider.getAccessToken(audience));
            return execution.execute(request, body);
        }
        return response;
    }
}
