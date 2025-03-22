package ru.kon.onlineshop.dto.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private int stockQuantity;
}
