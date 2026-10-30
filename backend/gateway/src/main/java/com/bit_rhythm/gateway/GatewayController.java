package com.bit_rhythm.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final String CORRELATION_ID = CorrelationIdFilter.HEADER;
    private static final String INTERNAL_API_KEY = "X-Internal-Api-Key";
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length",
            "forwarded", "x-forwarded-for", "x-forwarded-host", "x-forwarded-port", "x-forwarded-proto");

    private final RestClient restClient;
    private final GatewayProperties properties;

    public GatewayController(RestClient restClient, GatewayProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @RequestMapping(value = "/{service}/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.HEAD,
            RequestMethod.OPTIONS
    })
    public ResponseEntity<byte[]> proxy(@PathVariable String service,
                                        HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) {
        String serviceBaseUrl = properties.services().get(service);
        if (serviceBaseUrl == null || serviceBaseUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown gateway service");
        }

        URI target = buildTargetUri(service, serviceBaseUrl, request);
        String correlationId = correlationId(request);
        HttpHeaders requestHeaders = forwardedHeaders(request, correlationId);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        RestClient.RequestBodySpec requestSpec = restClient.method(method)
                .uri(target)
                .headers(headers -> headers.addAll(requestHeaders));

        RestClient.ResponseSpec responseSpec = body == null || body.length == 0
                ? requestSpec.retrieve()
                : requestSpec.body(body).retrieve();

        try {
            ResponseEntity<byte[]> upstreamResponse = responseSpec
                    .onStatus(status -> true, (upstream, ignored) -> {
                    })
                    .toEntity(byte[].class);
            return copyResponse(upstreamResponse, correlationId);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                    "Upstream service timed out or is unavailable", exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Upstream service request failed", exception);
        }
    }

    private URI buildTargetUri(String service, String serviceBaseUrl, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String prefix = request.getContextPath() + "/api/" + service;
        String path = requestUri.startsWith(prefix) ? requestUri.substring(prefix.length()) : "";
        if (path.isBlank()) {
            path = "/";
        } else if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String normalizedBase = serviceBaseUrl.endsWith("/")
                ? serviceBaseUrl.substring(0, serviceBaseUrl.length() - 1)
                : serviceBaseUrl;
        try {
            String query = request.getQueryString();
            return new URI(normalizedBase + path
                    + (query == null || query.isBlank() ? "" : "?" + query));
        } catch (URISyntaxException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request URI", exception);
        }
    }

    private HttpHeaders forwardedHeaders(HttpServletRequest request, String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        var names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(java.util.Locale.ROOT))
                    && !INTERNAL_API_KEY.equalsIgnoreCase(name)
                    && !CORRELATION_ID.equalsIgnoreCase(name)) {
                var values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    headers.add(name, values.nextElement());
                }
            }
        }
        headers.set(CORRELATION_ID, correlationId);
        if (properties.internalApiKey() != null && !properties.internalApiKey().isBlank()) {
            headers.set(INTERNAL_API_KEY, properties.internalApiKey());
        }
        return headers;
    }

    private ResponseEntity<byte[]> copyResponse(ResponseEntity<byte[]> response, String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        response.getHeaders().forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(java.util.Locale.ROOT))) {
                headers.put(name, values);
            }
        });
        headers.set(CORRELATION_ID, correlationId);
        return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
    }

    private String correlationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute(CorrelationIdFilter.ATTRIBUTE);
        return correlationId instanceof String id ? id : java.util.UUID.randomUUID().toString();
    }
}
