package com.enterprise.shop.middleware.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {
    private String requestId;
    private String callerService;
    private String endpoint;
    private String method;
    private String protocolType;
    private String result;
    private String clientCertCN;
    private LocalDateTime timestamp;
    private long durationMs;
    private String errorMessage;
}
