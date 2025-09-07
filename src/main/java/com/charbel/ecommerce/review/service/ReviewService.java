package com.charbel.ecommerce.review.service;

import com.charbel.ecommerce.exception.DuplicateReviewException;
import com.charbel.ecommerce.exception.ReviewNotFoundException;
import com.charbel.ecommerce.exception.UnauthorizedReviewAccessException;
import com.charbel.ecommerce.orders.repository.OrderRepository;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.review.dto.*;
import com.charbel.ecommerce.review.entity.Review;
import com.charbel.ecommerce.review.repository.ReviewRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse createReview(UUID productId, UUID userId, CreateReviewRequest request) {
        log.info("Creating review for product {} by user {}", productId, userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new DuplicateReviewException("You have already reviewed this product");
        }

        boolean hasVerifiedPurchase = orderRepository.existsByUserIdAndProductId(userId, productId);

        Review review = Review.builder()
                .product(product)
                .productId(productId)
                .user(user)
                .userId(userId)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isVerifiedPurchase(hasVerifiedPurchase)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with ID: {}", savedReview.getId());

        // Update product's average rating and review count
        updateProductRatingStats(productId);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UUID userId, UpdateReviewRequest request) {
        log.info("Updating review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewAccessException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        log.info("Review updated successfully: {}", reviewId);

        // Update product's average rating and review count
        updateProductRatingStats(review.getProductId());

        return ReviewResponse.fromEntity(savedReview);
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        log.info("Deleting review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedReviewAccessException("You can only delete your own reviews");
        }

        UUID productId = review.getProductId();
        reviewRepository.delete(review);
        log.info("Review deleted successfully: {}", reviewId);

        // Update product's average rating and review count
        updateProductRatingStats(productId);
    }

    @Transactional(readOnly = true)
    public PaginatedReviewsResponse getProductReviews(UUID productId, Pageable pageable, Integer rating, String sortBy) {
        log.info("Fetching reviews for product: {} with rating filter: {}", productId, rating);

        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product not found with ID: " + productId);
        }

        Page<Review> reviewPage;

        if (rating != null) {
            reviewPage = reviewRepository.findByProductIdAndRatingOrderByCreatedAtDesc(productId, rating, pageable);
        } else if ("helpful".equals(sortBy)) {
            reviewPage = reviewRepository.findByProductIdOrderByHelpfulCountDescAndCreatedAtDesc(productId, pageable);
        } else {
            reviewPage = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        }

        List<ReviewResponse> reviews = reviewPage.getContent().stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        ReviewSummaryResponse summary = getReviewSummary(productId);

        return PaginatedReviewsResponse.builder()
                .reviews(reviews)
                .currentPage(reviewPage.getNumber())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .hasNext(reviewPage.hasNext())
                .hasPrevious(reviewPage.hasPrevious())
                .summary(summary)
                .build();
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(UUID productId) {
        log.debug("Getting review summary for product: {}", productId);

        Long totalReviews = reviewRepository.countByProductId(productId);
        BigDecimal averageRating = reviewRepository.findAverageRatingByProductId(productId);
        
        if (averageRating != null) {
            averageRating = averageRating.setScale(1, RoundingMode.HALF_UP);
        }

        List<Object[]> distribution = reviewRepository.findRatingDistributionByProductId(productId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDistribution.put(rating, count);
        }

        return ReviewSummaryResponse.create(totalReviews, averageRating, ratingDistribution);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(UUID userId, Pageable pageable) {
        log.info("Fetching reviews for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }

        Page<Review> reviewPage = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return reviewPage.map(ReviewResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<ReviewResponse> getUserReviewForProduct(UUID productId, UUID userId) {
        log.debug("Getting user review for product: {} and user: {}", productId, userId);

        Optional<Review> review = reviewRepository.findByProductIdAndUserId(productId, userId);
        return review.map(ReviewResponse::fromEntity);
    }

    @Transactional
    public ReviewResponse markReviewHelpful(UUID reviewId) {
        log.info("Marking review as helpful: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        Review savedReview = reviewRepository.save(review);

        log.info("Review helpful count updated to: {}", savedReview.getHelpfulCount());
        return ReviewResponse.fromEntity(savedReview);
    }

    @Transactional
    private void updateProductRatingStats(UUID productId) {
        log.debug("Updating rating stats for product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));

        Long reviewCount = reviewRepository.countByProductId(productId);
        BigDecimal averageRating = reviewRepository.findAverageRatingByProductId(productId);

        if (averageRating != null) {
            averageRating = averageRating.setScale(1, RoundingMode.HALF_UP);
        }

        product.setReviewCount(reviewCount);
        product.setAverageRating(averageRating);

        productRepository.save(product);
        log.debug("Updated product {} rating stats: count={}, average={}", productId, reviewCount, averageRating);
    }
}