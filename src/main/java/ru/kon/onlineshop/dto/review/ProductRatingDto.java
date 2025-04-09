package ru.kon.onlineshop.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRatingDto {
    private Long productId;
    private Double averageRating;
    private Long reviewCount;
}
