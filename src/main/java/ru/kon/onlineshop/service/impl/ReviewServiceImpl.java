package ru.kon.onlineshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kon.onlineshop.dto.review.CreateReviewRequest;
import ru.kon.onlineshop.dto.review.ProductRatingDto;
import ru.kon.onlineshop.dto.review.ReviewDto;
import ru.kon.onlineshop.entity.*;
import ru.kon.onlineshop.mapper.ReviewMapper;
import ru.kon.onlineshop.repository.ProductRepository;
import ru.kon.onlineshop.repository.ReviewRepository;
import ru.kon.onlineshop.repository.UserRepository;
import ru.kon.onlineshop.security.model.UserDetailsImpl;
import ru.kon.onlineshop.service.PhotoStorageService;
import ru.kon.onlineshop.service.ReviewService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PhotoStorageService photoStorageService;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewDto addReview(Long productId, CreateReviewRequest request, List<MultipartFile> photos) throws IOException {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id:" + userDetails.getId() + "не найден"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Продукт с id: " + productId + "не найден"));

        if (reviewRepository.findByUserIdAndProductId(user.getId(), productId).isPresent()) {
            throw new IllegalArgumentException("У пользователя уже есть отзыв на этот продукт");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        if (photos != null && !photos.isEmpty()) {
            String subfolder = "reviews/" + productId;
            List<String> photoUrls = photoStorageService.storePhotos(photos, subfolder);

            for (String url : photoUrls) {
                ReviewPhoto reviewPhoto = ReviewPhoto.builder()
                        .imageUrl(url)
                        .review(review)
                        .build();
                review.addPhoto(reviewPhoto);
            }
        }

        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toReviewDto(savedReview);
    }

    @Override
    @Transactional
    public Page<ReviewDto> getReviewsForProduct(Long productId, Integer ratingFilter, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Продукт с id: " + productId + "не найден");
        }

        Page<Review> reviewsPage;
        if (ratingFilter != null) {
            reviewsPage = reviewRepository.findByProductIdAndRating(productId, ratingFilter, pageable);
        } else {
            reviewsPage = reviewRepository.findByProductId(productId, pageable);
        }

        return reviewsPage.map(reviewMapper::toReviewDto);
    }

    @Override
    @Transactional
    public ProductRatingDto getProductAverageRating(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Продукт с id: " + productId + "не найден");
        }
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId).orElse(0.0);
        Long reviewCount = reviewRepository.countByProductId(productId);

        return ProductRatingDto.builder()
                .productId(productId)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) throws IOException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Отзыв с id: " + reviewId + "не найден"));

        for (ReviewPhoto photo : review.getPhotos()) {
            try {
                photoStorageService.deletePhoto(photo.getImageUrl());
            } catch (IOException e) {
                System.err.println("Ошибка при удалении фото отзыва: " + photo.getImageUrl() + " - " + e.getMessage());
            }
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public void deleteReviewPhoto(Long reviewId, Long photoId) throws IOException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Отзыв с id: " + reviewId + "не найден"));

        ReviewPhoto photoToDelete = review.getPhotos().stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Фото с ID: " + photoId + " и отзывом с id: " + reviewId + "не найдено"));

          photoStorageService.deletePhoto(photoToDelete.getImageUrl());

        review.removePhoto(photoToDelete);
        reviewRepository.save(review);
    }

    private UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new AccessDeniedException("Пользователь не авторизован");
        }
        return (UserDetailsImpl) authentication.getPrincipal();
    }
}


