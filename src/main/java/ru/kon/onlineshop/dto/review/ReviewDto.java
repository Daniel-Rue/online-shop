package ru.kon.onlineshop.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private UserSummaryDto user;
    private Long productId;
    private Integer rating;
    private String comment;
    private List<ReviewPhotoDto> photos;
    private Instant createdAt;
    private Instant updatedAt;
}
