package ru.kon.onlineshop.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private BigDecimal totalAmount;
    private Instant createdAt;
}