package ru.kon.onlineshop.dto.product;

import lombok.*;
import ru.kon.onlineshop.dto.category.CategoryDto;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private int stockQuantity;
    @Singular
    private Set<Long> categoryIds;
}
