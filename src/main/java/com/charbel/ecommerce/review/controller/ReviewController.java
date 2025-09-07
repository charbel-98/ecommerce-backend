package com.charbel.ecommerce.review.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.charbel.ecommerce.review.dto.CreateReviewRequest;
import com.charbel.ecommerce.review.dto.PaginatedReviewsResponse;
import com.charbel.ecommerce.review.dto.ReviewResponse;
import com.charbel.ecommerce.review.dto.ReviewSummaryResponse;
import com.charbel.ecommerce.review.dto.UpdateReviewRequest;
import com.charbel.ecommerce.review.service.ReviewService;
import com.charbel.ecommerce.service.SecurityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityService securityService;

    @PostMapping(value = "/products/{productId}/reviews", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID productId,
            @Valid @ModelAttribute CreateReviewRequest request) {

        log.info("Creating review for product: {} with {} images", productId,
                request.getImages() != null ? request.getImages().length : 0);
        UUID userId = securityService.getCurrentUserId();

        ReviewResponse response = reviewService.createReview(productId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<PaginatedReviewsResponse> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) Boolean images) {

        log.info("Fetching reviews for product: {} (page: {}, size: {}, rating: {}, sortBy: {}, images: {})",
                productId, page, size, rating, sortBy, images);

        Pageable pageable = PageRequest.of(page, size);

        // Try to get current user ID if authenticated, but don't require authentication
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception e) {
            // User is not authenticated, which is fine for public review endpoint
            log.debug("No authenticated user found for review fetch");
        }

        PaginatedReviewsResponse response = reviewService.getProductReviews(productId, pageable, rating, sortBy, images,
                currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ResponseEntity<ReviewSummaryResponse> getProductReviewSummary(@PathVariable UUID productId) {
        log.info("Fetching review summary for product: {}", productId);
        ReviewSummaryResponse response = reviewService.getReviewSummary(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}/reviews/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> getCurrentUserReview(@PathVariable UUID productId) {
        log.info("Fetching current user review for product: {}", productId);
        UUID userId = securityService.getCurrentUserId();

        Optional<ReviewResponse> response = reviewService.getUserReviewForProduct(productId, userId);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {

        log.info("Updating review: {}", reviewId);
        UUID userId = securityService.getCurrentUserId();

        ReviewResponse response = reviewService.updateReview(reviewId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        log.info("Deleting review: {}", reviewId);
        UUID userId = securityService.getCurrentUserId();

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> markReviewHelpful(@PathVariable UUID reviewId) {
        log.info("Marking review as helpful: {}", reviewId);
        UUID userId = securityService.getCurrentUserId();

        ReviewResponse response = reviewService.markReviewHelpful(reviewId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews/my")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewResponse>> getCurrentUserReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching current user reviews (page: {}, size: {})", page, size);
        UUID userId = securityService.getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> response = reviewService.getUserReviews(userId, pageable);
        return ResponseEntity.ok(response);
    }
}