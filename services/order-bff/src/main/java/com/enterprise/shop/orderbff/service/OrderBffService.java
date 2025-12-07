package com.enterprise.shop.orderbff.service;

import com.enterprise.shop.orderbff.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBffService {
    
    private final WebClient middlewareWebClient;
    
    public Mono<String> addToCart(AddToCartRequest request) {
        log.info("BFF: Adding product {} to cart for user {}", request.getProductId(), request.getUserId());
        
        return middlewareWebClient
                .post()
                .uri("/api/order/add-to-cart")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Added to cart successfully"))
                .doOnError(error -> log.error("Add to cart failed: {}", error.getMessage()));
    }
    
    public Mono<String> getCart(Long userId) {
        log.info("BFF: Getting cart for user {}", userId);
        
        return middlewareWebClient
                .get()
                .uri("/api/order/cart/{userId}", userId)
                .retrieve()
                .bodyToMono(String.class);
    }
    
    public Mono<String> submitOrder(OrderSubmitRequest request) {
        log.info("BFF: Submitting order for user {}", request.getUserId());
        
        return middlewareWebClient
                .post()
                .uri("/api/order/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Order submitted successfully"))
                .doOnError(error -> log.error("Order submission failed: {}", error.getMessage()));
    }
    
    public Mono<String> processPayment(PaymentRequest request) {
        log.info("BFF: Processing payment for order {}", request.getOrderId());
        
        return middlewareWebClient
                .post()
                .uri("/api/order/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.info("Payment processed successfully"))
                .doOnError(error -> log.error("Payment failed: {}", error.getMessage()));
    }
    
    public Mono<String> trackOrder(Long orderId) {
        log.info("BFF: Tracking order {}", orderId);
        
        return middlewareWebClient
                .get()
                .uri("/api/order/track/{orderId}", orderId)
                .retrieve()
                .bodyToMono(String.class);
    }
    
    public Mono<String> getUserOrders(Long userId) {
        log.info("BFF: Getting orders for user {}", userId);
        
        return middlewareWebClient
                .get()
                .uri("/api/order/user/{userId}", userId)
                .retrieve()
                .bodyToMono(String.class);
    }
    
    public Mono<String> getProducts() {
        log.info("BFF: Getting products");
        
        return middlewareWebClient
                .get()
                .uri("/api/product")
                .retrieve()
                .bodyToMono(String.class);
    }
    
    public Mono<String> getProduct(Long productId) {
        log.info("BFF: Getting product {}", productId);
        
        return middlewareWebClient
                .get()
                .uri("/api/product/{id}", productId)
                .retrieve()
                .bodyToMono(String.class);
    }
}
