package ru.kon.onlineshop.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class CreateProductRequest {
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private int stockQuantity;
    private Set<Long> categoryIds;
}
