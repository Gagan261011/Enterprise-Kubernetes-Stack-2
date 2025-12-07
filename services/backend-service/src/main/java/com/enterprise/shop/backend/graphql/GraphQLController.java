package com.enterprise.shop.backend.graphql;

import com.enterprise.shop.backend.dto.*;
import com.enterprise.shop.backend.model.*;
import com.enterprise.shop.backend.service.OrderService;
import com.enterprise.shop.backend.service.ProductService;
import com.enterprise.shop.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GraphQLController {
    
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    
    // Queries
    @QueryMapping
    public List<Product> products() {
        return productService.getAllProducts();
    }
    
    @QueryMapping
    public Product product(@Argument Long id) {
        return productService.getProductById(id);
    }
    
    @QueryMapping
    public User user(@Argument Long id) {
        return userService.getUserById(id);
    }
    
    @QueryMapping
    public Cart cart(@Argument Long userId) {
        return orderService.getCart(userId);
    }
    
    @QueryMapping
    public List<Order> orders(@Argument Long userId) {
        return orderService.getUserOrders(userId);
    }
    
    @QueryMapping
    public OrderTrackingResponse orderTracking(@Argument Long orderId) {
        return orderService.trackOrder(orderId);
    }
    
    // Mutations
    @MutationMapping
    public User register(@Argument UserRegistrationRequest input) {
        return userService.register(input);
    }
    
    @MutationMapping
    public LoginResponse login(@Argument String email, @Argument String password) {
        return userService.login(LoginRequest.builder()
                .email(email)
                .password(password)
                .build());
    }
    
    @MutationMapping
    public Cart addToCart(@Argument Long userId, @Argument Long productId, @Argument Integer quantity) {
        return orderService.addToCart(AddToCartRequest.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .build());
    }
    
    @MutationMapping
    public Order submitOrder(@Argument Long userId, @Argument String shippingAddress) {
        return orderService.submitOrder(OrderSubmitRequest.builder()
                .userId(userId)
                .shippingAddress(shippingAddress)
                .build());
    }
    
    @MutationMapping
    public PaymentResponse processPayment(
            @Argument Long orderId,
            @Argument String cardNumber,
            @Argument String cardHolderName,
            @Argument String expiryDate,
            @Argument String cvv) {
        return orderService.processPayment(PaymentRequest.builder()
                .orderId(orderId)
                .cardNumber(cardNumber)
                .cardHolderName(cardHolderName)
                .expiryDate(expiryDate)
                .cvv(cvv)
                .build());
    }
}
