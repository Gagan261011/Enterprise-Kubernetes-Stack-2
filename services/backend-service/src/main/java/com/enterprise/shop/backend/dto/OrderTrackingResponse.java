package com.enterprise.shop.backend.dto;

import com.enterprise.shop.backend.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private Long orderId;
    private Order.OrderStatus currentStatus;
    private List<TrackingEvent> timeline;
    private String estimatedDelivery;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEvent {
        private Order.OrderStatus status;
        private LocalDateTime timestamp;
        private String description;
        private boolean completed;
    }
}
