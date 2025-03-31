package ru.kon.onlineshop.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private int stockQuantity;
}