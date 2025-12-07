package com.enterprise.shop.userbff.controller;

import com.enterprise.shop.userbff.dto.*;
import com.enterprise.shop.userbff.service.UserBffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserBffService userBffService;
    
    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@Valid @RequestBody UserRegistrationRequest request) {
        return userBffService.register(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@Valid @RequestBody LoginRequest request) {
        return userBffService.login(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @PutMapping("/profile/{userId}")
    public Mono<ResponseEntity<String>> updateProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileRequest request) {
        return userBffService.updateProfile(userId, request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @PutMapping("/password/{userId}")
    public Mono<ResponseEntity<String>> updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordUpdateRequest request) {
        return userBffService.updatePassword(userId, request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<String>> getUser(@PathVariable Long userId) {
        return userBffService.getUser(userId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
}
