package com.enterprise.shop.orderbff.controller;

import com.enterprise.shop.orderbff.service.OrderBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {
    
    private final OrderBffService orderBffService;
    
    @GetMapping
    public Mono<ResponseEntity<String>> getProducts() {
        return orderBffService.getProducts()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
    
    @GetMapping("/{productId}")
    public Mono<ResponseEntity<String>> getProduct(@PathVariable Long productId) {
        return orderBffService.getProduct(productId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(
                        ResponseEntity.badRequest().body("{\"error\": \"" + error.getMessage() + "\"}")));
    }
}
