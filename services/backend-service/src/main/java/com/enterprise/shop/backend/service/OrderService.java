package com.enterprise.shop.backend.service;

import com.enterprise.shop.backend.dto.*;
import com.enterprise.shop.backend.model.*;
import com.enterprise.shop.backend.repository.CartRepository;
import com.enterprise.shop.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;
    
    @Transactional
    public Cart addToCart(AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElse(Cart.builder()
                        .userId(request.getUserId())
                        .items(new ArrayList<>())
                        .build());
        
        Product product = productService.getProductById(request.getProductId());
        
        // Check if product already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            cart.getItems().add(newItem);
        }
        
        return cartRepository.save(cart);
    }
    
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());
    }
    
    @Transactional
    public Order submitOrder(OrderSubmitRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart is empty"));
        
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .totalPrice(cartItem.getTotalPrice())
                        .build())
                .toList();
        
        Order order = Order.builder()
                .userId(request.getUserId())
                .items(new ArrayList<>(orderItems))
                .totalAmount(cart.getTotalAmount())
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .paymentStatus("PENDING")
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after order
        cart.getItems().clear();
        cartRepository.save(cart);
        
        return savedOrder;
    }
    
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Simulate payment processing
        boolean paymentSuccess = simulatePayment(request);
        
        if (paymentSuccess) {
            order.setPaymentStatus("COMPLETED");
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            return PaymentResponse.builder()
                    .orderId(order.getId())
                    .transactionId(UUID.randomUUID().toString())
                    .status("SUCCESS")
                    .message("Payment processed successfully")
                    .build();
        } else {
            order.setPaymentStatus("FAILED");
            orderRepository.save(order);
            
            return PaymentResponse.builder()
                    .orderId(order.getId())
                    .transactionId(null)
                    .status("FAILED")
                    .message("Payment processing failed")
                    .build();
        }
    }
    
    public OrderTrackingResponse trackOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        List<OrderTrackingResponse.TrackingEvent> timeline = generateTimeline(order);
        
        return OrderTrackingResponse.builder()
                .orderId(order.getId())
                .currentStatus(order.getStatus())
                .timeline(timeline)
                .estimatedDelivery(calculateEstimatedDelivery(order))
                .build();
    }
    
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    private boolean simulatePayment(PaymentRequest request) {
        // Dummy payment - always succeeds unless card number ends with 0
        return request.getCardNumber() == null || 
               !request.getCardNumber().endsWith("0");
    }
    
    private List<OrderTrackingResponse.TrackingEvent> generateTimeline(Order order) {
        List<OrderTrackingResponse.TrackingEvent> timeline = new ArrayList<>();
        LocalDateTime orderTime = order.getCreatedAt();
        
        Order.OrderStatus[] statuses = Order.OrderStatus.values();
        int currentStatusOrdinal = order.getStatus().ordinal();
        
        for (int i = 0; i <= Math.min(currentStatusOrdinal, 4); i++) {
            Order.OrderStatus status = statuses[i];
            timeline.add(OrderTrackingResponse.TrackingEvent.builder()
                    .status(status)
                    .timestamp(orderTime.plusHours(i * 24))
                    .description(getStatusDescription(status))
                    .completed(i <= currentStatusOrdinal)
                    .build());
        }
        
        // Add future statuses as not completed
        for (int i = currentStatusOrdinal + 1; i <= 4; i++) {
            Order.OrderStatus status = statuses[i];
            timeline.add(OrderTrackingResponse.TrackingEvent.builder()
                    .status(status)
                    .timestamp(null)
                    .description(getStatusDescription(status))
                    .completed(false)
                    .build());
        }
        
        return timeline;
    }
    
    private String getStatusDescription(Order.OrderStatus status) {
        return switch (status) {
            case PENDING -> "Order placed, awaiting payment";
            case CONFIRMED -> "Payment confirmed, preparing order";
            case PROCESSING -> "Order is being prepared";
            case SHIPPED -> "Order has been shipped";
            case DELIVERED -> "Order delivered";
            case CANCELLED -> "Order cancelled";
        };
    }
    
    private String calculateEstimatedDelivery(Order order) {
        return order.getCreatedAt().plusDays(5).toLocalDate().toString();
    }
}
