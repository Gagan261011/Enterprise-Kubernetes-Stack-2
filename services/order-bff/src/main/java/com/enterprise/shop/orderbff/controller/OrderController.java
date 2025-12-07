package com.enterprise.shop.orderbff.controller;

import com.enterprise.shop.orderbff.dto.*;
import com.enterprise.shop.orderbff.service.OrderBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderBffService orderBffService;
    
    @PostMapping("/add-to-cart")
    public Mono<ResponseEntity<String>> addToCart(@RequestBody AddToCartRequest request) {
        return orderBffService.addToCart(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @GetMapping("/cart/{userId}")
    public Mono<ResponseEntity<String>> getCart(@PathVariable Long userId) {
        return orderBffService.getCart(userId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @PostMapping("/submit")
    public Mono<ResponseEntity<String>> submitOrder(@RequestBody OrderSubmitRequest request) {
        return orderBffService.submitOrder(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @PostMapping("/payment")
    public Mono<ResponseEntity<String>> processPayment(@RequestBody PaymentRequest request) {
        return orderBffService.processPayment(request)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @GetMapping("/track/{orderId}")
    public Mono<ResponseEntity<String>> trackOrder(@PathVariable Long orderId) {
        return orderBffService.trackOrder(orderId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<String>> getUserOrders(@PathVariable Long userId) {
        return orderBffService.getUserOrders(userId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
}
