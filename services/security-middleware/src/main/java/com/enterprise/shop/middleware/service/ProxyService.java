package com.enterprise.shop.middleware.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyService {
    
    private final WebClient backendWebClient;
    private final CertificateValidationService validationService;
    
    /**
     * Forward request to backend service
     */
    public Mono<String> forwardRequest(String path, HttpMethod method, 
                                        String body, HttpHeaders headers,
                                        String callerService) {
        long startTime = System.currentTimeMillis();
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        String protocolType = validationService.detectProtocolType(contentType, path);
        
        log.debug("Forwarding {} request to backend: {} {}", protocolType, method, path);
        
        WebClient.RequestBodySpec requestSpec = backendWebClient
                .method(method)
                .uri(path)
                .headers(h -> {
                    // Copy relevant headers
                    if (contentType != null) {
                        h.setContentType(MediaType.parseMediaType(contentType));
                    }
                    String accept = headers.getFirst(HttpHeaders.ACCEPT);
                    if (accept != null) {
                        h.set(HttpHeaders.ACCEPT, accept);
                    }
                    // Add caller service header for backend tracking
                    h.set("X-Caller-Service", callerService);
                    h.set("X-Protocol-Type", protocolType);
                });
        
        Mono<String> responseMono;
        
        if (body != null && !body.isEmpty() && 
            (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
            responseMono = requestSpec
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class);
        } else {
            responseMono = requestSpec
                    .retrieve()
                    .bodyToMono(String.class);
        }
        
        return responseMono
                .doOnSuccess(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    validationService.logRequest(
                            callerService, path, method.name(), 
                            protocolType, "ALLOWED", callerService,
                            duration, null);
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    validationService.logRequest(
                            callerService, path, method.name(),
                            protocolType, "ERROR", callerService,
                            duration, error.getMessage());
                });
    }
}
