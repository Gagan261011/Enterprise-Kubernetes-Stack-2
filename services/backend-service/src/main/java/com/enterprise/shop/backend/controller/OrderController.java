package com.enterprise.shop.backend.controller;

import com.enterprise.shop.backend.dto.*;
import com.enterprise.shop.backend.model.Cart;
import com.enterprise.shop.backend.model.Order;
import com.enterprise.shop.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping("/add-to-cart")
    public ResponseEntity<ApiResponse<Cart>> addToCart(@RequestBody AddToCartRequest request) {
        try {
            Cart cart = orderService.addToCart(request);
            return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/cart/{userId}")
    public ResponseEntity<ApiResponse<Cart>> getCart(@PathVariable Long userId) {
        Cart cart = orderService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
    
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Order>> submitOrder(@RequestBody OrderSubmitRequest request) {
        try {
            Order order = orderService.submitOrder(request);
            return ResponseEntity.ok(ApiResponse.success("Order submitted successfully", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = orderService.processPayment(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/track/{orderId}")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> trackOrder(@PathVariable Long orderId) {
        try {
            OrderTrackingResponse tracking = orderService.trackOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success(tracking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}
