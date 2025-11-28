package ru.kon.onlineshop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kon.onlineshop.dto.review.CreateReviewRequest;
import ru.kon.onlineshop.dto.review.ProductRatingDto;
import ru.kon.onlineshop.dto.review.ReviewDto;
import ru.kon.onlineshop.service.ReviewService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(value = "/products/{productId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReviewDto> addReview(
            @PathVariable Long productId,
            @Valid @RequestPart("review") CreateReviewRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) throws IOException {

        ReviewDto createdReview = reviewService.addReview(productId, request, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewDto>> getReviewsForProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer ratingFilter,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewDto> reviewsPage = reviewService.getReviewsForProduct(productId, ratingFilter, pageable);
        return ResponseEntity.ok(reviewsPage);
    }

    @GetMapping("/products/{productId}/rating")
    public ResponseEntity<ProductRatingDto> getProductAverageRating(@PathVariable Long productId) {
        ProductRatingDto ratingDto = reviewService.getProductAverageRating(productId);
        return ResponseEntity.ok(ratingDto);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) throws IOException {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/{reviewId}/photos/{photoId}")
    public ResponseEntity<Void> deleteReviewPhoto(
            @PathVariable Long reviewId,
            @PathVariable Long photoId) throws IOException {
        reviewService.deleteReviewPhoto(reviewId, photoId);
        return ResponseEntity.noContent().build();
    }
}
