package com.enterprise.shop.backend.soap;

import com.enterprise.shop.backend.dto.*;
import com.enterprise.shop.backend.model.Cart;
import com.enterprise.shop.backend.model.Order;
import com.enterprise.shop.backend.service.OrderService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@WebService(serviceName = "OrderService")
@Service
@RequiredArgsConstructor
public class OrderSoapService {
    
    private final OrderService orderService;
    
    @WebMethod
    public Cart addToCart(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "productId") Long productId,
            @WebParam(name = "quantity") Integer quantity) {
        
        AddToCartRequest request = AddToCartRequest.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity)
                .build();
        
        return orderService.addToCart(request);
    }
    
    @WebMethod
    public Cart getCart(@WebParam(name = "userId") Long userId) {
        return orderService.getCart(userId);
    }
    
    @WebMethod
    public Order submitOrder(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "shippingAddress") String shippingAddress) {
        
        OrderSubmitRequest request = OrderSubmitRequest.builder()
                .userId(userId)
                .shippingAddress(shippingAddress)
                .build();
        
        return orderService.submitOrder(request);
    }
    
    @WebMethod
    public PaymentResponse processPayment(
            @WebParam(name = "orderId") Long orderId,
            @WebParam(name = "amount") BigDecimal amount,
            @WebParam(name = "cardNumber") String cardNumber,
            @WebParam(name = "cardHolderName") String cardHolderName,
            @WebParam(name = "expiryDate") String expiryDate,
            @WebParam(name = "cvv") String cvv) {
        
        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .amount(amount)
                .cardNumber(cardNumber)
                .cardHolderName(cardHolderName)
                .expiryDate(expiryDate)
                .cvv(cvv)
                .build();
        
        return orderService.processPayment(request);
    }
    
    @WebMethod
    public OrderTrackingResponse trackOrder(@WebParam(name = "orderId") Long orderId) {
        return orderService.trackOrder(orderId);
    }
    
    @WebMethod
    public List<Order> getUserOrders(@WebParam(name = "userId") Long userId) {
        return orderService.getUserOrders(userId);
    }
}
