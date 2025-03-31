package ru.kon.onlineshop.dto.product;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private int stockQuantity;
}
