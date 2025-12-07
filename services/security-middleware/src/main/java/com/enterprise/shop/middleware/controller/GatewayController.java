package com.enterprise.shop.middleware.controller;

import com.enterprise.shop.middleware.service.CertificateValidationService;
import com.enterprise.shop.middleware.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class GatewayController {
    
    private final ProxyService proxyService;
    private final CertificateValidationService validationService;
    
    /**
     * Catch-all handler that validates mTLS and forwards to backend
     */
    @RequestMapping(value = "/**", method = {
            RequestMethod.GET, RequestMethod.POST, 
            RequestMethod.PUT, RequestMethod.DELETE,
            RequestMethod.PATCH
    })
    public Mono<ResponseEntity<String>> handleRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @RequestHeader HttpHeaders headers) {
        
        // Extract client certificate CN
        String clientCertCN = extractClientCertificateCN(request);
        
        // For demo/non-mTLS mode, use header-based service identification
        if (clientCertCN == null) {
            clientCertCN = headers.getFirst("X-Client-Service");
            if (clientCertCN == null) {
                clientCertCN = "unknown";
            }
        }
        
        // Validate client certificate
        boolean isValid = validationService.validateClientCertificate(clientCertCN);
        
        // In demo mode, allow all requests but log validation status
        if (!isValid) {
            log.warn("Request from untrusted service: {}, but allowing in demo mode", clientCertCN);
        }
        
        // Get the path after /api
        String fullPath = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null) {
            fullPath = fullPath + "?" + queryString;
        }
        
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        
        return proxyService.forwardRequest(fullPath, method, body, headers, clientCertCN)
                .map(response -> ResponseEntity.ok()
                        .header("X-Proxied-By", "security-middleware")
                        .header("X-Client-Validated", String.valueOf(isValid))
                        .body(response))
                .onErrorResume(error -> {
                    log.error("Error forwarding request: {}", error.getMessage());
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.BAD_GATEWAY)
                            .body("{\"error\": \"" + error.getMessage() + "\"}"));
                });
    }
    
    /**
     * Extract CN from client certificate
     */
    private String extractClientCertificateCN(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute(
                "jakarta.servlet.request.X509Certificate");
        
        if (certs != null && certs.length > 0) {
            String dn = certs[0].getSubjectX500Principal().getName();
            // Extract CN from DN
            for (String part : dn.split(",")) {
                if (part.trim().startsWith("CN=")) {
                    return part.trim().substring(3);
                }
            }
        }
        return null;
    }
}
