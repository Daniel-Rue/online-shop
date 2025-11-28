package ru.kon.onlineshop.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderItemDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtOrder;
}
