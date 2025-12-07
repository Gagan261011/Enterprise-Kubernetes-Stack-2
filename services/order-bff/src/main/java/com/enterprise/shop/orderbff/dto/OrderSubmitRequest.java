package com.enterprise.shop.orderbff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSubmitRequest {
    private Long userId;
    private String shippingAddress;
}
