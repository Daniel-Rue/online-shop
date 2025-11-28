package ru.kon.onlineshop.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private Instant createdAt;
}