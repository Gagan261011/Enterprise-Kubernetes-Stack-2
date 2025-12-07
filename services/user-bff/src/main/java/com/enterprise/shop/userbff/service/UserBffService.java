package com.enterprise.shop.userbff.service;

import com.enterprise.shop.userbff.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBffService {
    
    private final WebClient middlewareWebClient;
    private final ObjectMapper objectMapper;
    
    public Mono<String> register(UserRegistrationRequest request) {
        log.info("BFF: Processing registration for {}", request.getEmail());
        
        return middlewareWebClient
                .post()
                .uri("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Registration successful for {}", request.getEmail()))
                .doOnError(error -> log.error("Registration failed: {}", error.getMessage()));
    }
    
    public Mono<String> login(LoginRequest request) {
        log.info("BFF: Processing login for {}", request.getEmail());
        
        return middlewareWebClient
                .post()
                .uri("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Login successful for {}", request.getEmail()))
                .doOnError(error -> log.error("Login failed: {}", error.getMessage()));
    }
    
    public Mono<String> updateProfile(Long userId, UserProfileRequest request) {
        log.info("BFF: Updating profile for user {}", userId);
        
        return middlewareWebClient
                .put()
                .uri("/api/user/profile/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Profile updated for user {}", userId))
                .doOnError(error -> log.error("Profile update failed: {}", error.getMessage()));
    }
    
    public Mono<String> updatePassword(Long userId, PasswordUpdateRequest request) {
        log.info("BFF: Updating password for user {}", userId);
        
        return middlewareWebClient
                .put()
                .uri("/api/user/password/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Password updated for user {}", userId))
                .doOnError(error -> log.error("Password update failed: {}", error.getMessage()));
    }
    
    public Mono<String> getUser(Long userId) {
        log.info("BFF: Getting user {}", userId);
        
        return middlewareWebClient
                .get()
                .uri("/api/user/{userId}", userId)
                .retrieve()
                .bodyToMono(String.class);
    }
}
