package com.enterprise.shop.middleware.controller;

import com.enterprise.shop.middleware.model.RequestLog;
import com.enterprise.shop.middleware.service.CertificateValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final CertificateValidationService validationService;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("requestsByService", validationService.getRequestStats());
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/logs")
    public ResponseEntity<List<RequestLog>> getLogs(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(validationService.getRecentLogs(limit));
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "security-middleware");
        return ResponseEntity.ok(health);
    }
}
