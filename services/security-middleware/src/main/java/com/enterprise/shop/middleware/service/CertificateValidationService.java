package com.enterprise.shop.middleware.service;

import com.enterprise.shop.middleware.model.RequestLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CertificateValidationService {
    
    // Trusted service Common Names
    private static final Set<String> TRUSTED_SERVICES = Set.of(
        "user-bff",
        "order-bff",
        "security-middleware"
    );
    
    // In-memory log storage for demo purposes
    private final List<RequestLog> requestLogs = new ArrayList<>();
    private final ConcurrentHashMap<String, Integer> requestCounts = new ConcurrentHashMap<>();
    
    /**
     * Validate client certificate CN against trusted services
     */
    public boolean validateClientCertificate(String clientCertCN) {
        if (clientCertCN == null || clientCertCN.isEmpty()) {
            log.warn("No client certificate CN provided");
            return false;
        }
        
        boolean isValid = TRUSTED_SERVICES.contains(clientCertCN);
        
        if (isValid) {
            log.info("Client certificate validated: {}", clientCertCN);
            requestCounts.merge(clientCertCN, 1, Integer::sum);
        } else {
            log.warn("Untrusted client certificate: {}", clientCertCN);
        }
        
        return isValid;
    }
    
    /**
     * Log a request for audit purposes
     */
    public void logRequest(String callerService, String endpoint, String method, 
                          String protocolType, String result, String clientCertCN,
                          long durationMs, String errorMessage) {
        RequestLog requestLog = RequestLog.builder()
                .requestId(UUID.randomUUID().toString())
                .callerService(callerService)
                .endpoint(endpoint)
                .method(method)
                .protocolType(protocolType)
                .result(result)
                .clientCertCN(clientCertCN)
                .timestamp(LocalDateTime.now())
                .durationMs(durationMs)
                .errorMessage(errorMessage)
                .build();
        
        requestLogs.add(requestLog);
        
        // Log to console/file
        if ("ALLOWED".equals(result)) {
            log.info("Request {} | Service: {} | Endpoint: {} {} | Protocol: {} | Duration: {}ms",
                    result, callerService, method, endpoint, protocolType, durationMs);
        } else {
            log.warn("Request {} | Service: {} | Endpoint: {} {} | Protocol: {} | Error: {}",
                    result, callerService, method, endpoint, protocolType, errorMessage);
        }
    }
    
    /**
     * Get request statistics
     */
    public ConcurrentHashMap<String, Integer> getRequestStats() {
        return requestCounts;
    }
    
    /**
     * Get recent request logs
     */
    public List<RequestLog> getRecentLogs(int limit) {
        int size = requestLogs.size();
        int start = Math.max(0, size - limit);
        return new ArrayList<>(requestLogs.subList(start, size));
    }
    
    /**
     * Detect protocol type from request
     */
    public String detectProtocolType(String contentType, String endpoint) {
        if (contentType != null) {
            if (contentType.contains("application/soap+xml") || 
                contentType.contains("text/xml")) {
                return "SOAP";
            }
            if (contentType.contains("application/graphql") ||
                endpoint.contains("/graphql")) {
                return "GraphQL";
            }
        }
        return "REST";
    }
}
