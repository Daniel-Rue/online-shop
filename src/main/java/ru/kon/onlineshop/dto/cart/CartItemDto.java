package ru.kon.onlineshop.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal basePrice;
}
