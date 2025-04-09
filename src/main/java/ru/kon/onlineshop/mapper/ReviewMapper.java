package ru.kon.onlineshop.mapper;

import org.springframework.stereotype.Component;
import ru.kon.onlineshop.dto.review.ReviewDto;
import ru.kon.onlineshop.dto.review.ReviewPhotoDto;
import ru.kon.onlineshop.dto.review.UserSummaryDto;
import ru.kon.onlineshop.entity.Review;
import ru.kon.onlineshop.entity.ReviewPhoto;
import ru.kon.onlineshop.entity.User;

import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    public ReviewDto toReviewDto(Review review) {
        if (review == null) return null;
        return ReviewDto.builder()
                .id(review.getId())
                .user(toUserSummaryDto(review.getUser()))
                .productId(review.getProduct().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .photos(review.getPhotos().stream().map(this::toReviewPhotoDto).collect(Collectors.toList()))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    public ReviewPhotoDto toReviewPhotoDto(ReviewPhoto photo) {
        if (photo == null) return null;
        return ReviewPhotoDto.builder()
                .id(photo.getId())
                .imageUrl(photo.getImageUrl())
                .uploadedAt(photo.getUploadedAt())
                .build();
    }

    public UserSummaryDto toUserSummaryDto(User user) {
        if (user == null) return null;
        return UserSummaryDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
