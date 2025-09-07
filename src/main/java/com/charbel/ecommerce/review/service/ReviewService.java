package com.charbel.ecommerce.review.service;

import com.charbel.ecommerce.exception.DuplicateReviewException;
import com.charbel.ecommerce.exception.DuplicateHelpfulVoteException;
import com.charbel.ecommerce.exception.ReviewNotFoundException;
import com.charbel.ecommerce.exception.SelfHelpfulVoteException;
import com.charbel.ecommerce.exception.UnauthorizedReviewAccessException;
import com.charbel.ecommerce.orders.repository.OrderRepository;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.repository.ProductRepository;
import com.charbel.ecommerce.review.dto.*;
import com.charbel.ecommerce.review.entity.Review;
import com.charbel.ecommerce.review.entity.ReviewImage;
import com.charbel.ecommerce.review.entity.ReviewHelpfulVote;
import com.charbel.ecommerce.review.repository.ReviewRepository;
import com.charbel.ecommerce.review.repository.ReviewImageRepository;
import com.charbel.ecommerce.review.repository.ReviewHelpfulVoteRepository;
import com.charbel.ecommerce.user.entity.User;
import com.charbel.ecommerce.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

import com.charbel.ecommerce.cdn.service.CdnService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewHelpfulVoteRepository reviewHelpfulVoteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CdnService cdnService;

    @Transactional
    public ReviewResponse createReview(UUID productId, UUID userId, CreateReviewRequest request) {
        return createReview(productId, userId, request, null);
    }

    @Transactional
    public ReviewResponse createReview(UUID productId, UUID userId, CreateReviewRequest request, MultipartFile[] images) {
        log.info("Creating review for product {} by user {} with {} images", 
                productId, userId, images != null ? images.length : 0);

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

        // Handle image uploads if provided
        if (images != null && images.length > 0) {
            uploadAndSaveReviewImages(savedReview, images);
        }

        // Update product's average rating and review count
        updateProductRatingStats(productId);

        // Fetch the review with images loaded
        Review reviewWithImages = reviewRepository.findById(savedReview.getId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found after creation"));

        return ReviewResponse.fromEntity(reviewWithImages);
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

        // Delete review images from CDN first
        List<ReviewImage> reviewImages = reviewImageRepository.findByReviewIdOrderBySortOrderAsc(reviewId);
        for (ReviewImage reviewImage : reviewImages) {
            try {
                cdnService.deleteImageByUrl(reviewImage.getImageUrl());
                log.debug("Deleted review image from CDN: {}", reviewImage.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete review image from CDN: {}", reviewImage.getImageUrl(), e);
            }
        }

        // Delete helpful votes for this review
        reviewHelpfulVoteRepository.deleteByReviewId(reviewId);
        log.debug("Deleted helpful votes for review: {}", reviewId);

        reviewRepository.delete(review);
        log.info("Review deleted successfully: {}", reviewId);

        // Update product's average rating and review count
        updateProductRatingStats(productId);
    }

    @Transactional(readOnly = true)
    public PaginatedReviewsResponse getProductReviews(UUID productId, Pageable pageable, Integer rating, String sortBy) {
        return getProductReviews(productId, pageable, rating, sortBy, null);
    }

    @Transactional(readOnly = true)
    public PaginatedReviewsResponse getProductReviews(UUID productId, Pageable pageable, Integer rating, String sortBy, UUID currentUserId) {
        log.info("Fetching reviews for product: {} with rating filter: {} for user: {}", productId, rating, currentUserId);

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

        List<ReviewResponse> reviews;
        if (currentUserId != null) {
            // Get all review IDs to check vote status
            List<UUID> reviewIds = reviewPage.getContent().stream()
                    .map(Review::getId)
                    .collect(Collectors.toList());
            
            // Check which reviews the current user has voted for
            Set<UUID> votedReviewIds = new HashSet<>();
            for (UUID reviewId : reviewIds) {
                if (reviewHelpfulVoteRepository.existsByReviewIdAndUserId(reviewId, currentUserId)) {
                    votedReviewIds.add(reviewId);
                }
            }

            reviews = reviewPage.getContent().stream()
                    .map(review -> ReviewResponse.fromEntity(review, votedReviewIds.contains(review.getId())))
                    .collect(Collectors.toList());
        } else {
            reviews = reviewPage.getContent().stream()
                    .map(ReviewResponse::fromEntity)
                    .collect(Collectors.toList());
        }

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
    public ReviewResponse markReviewHelpful(UUID reviewId, UUID userId) {
        log.info("Marking review as helpful: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with ID: " + reviewId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Check if user is trying to mark their own review as helpful
        if (review.getUserId().equals(userId)) {
            throw new SelfHelpfulVoteException("You cannot mark your own review as helpful");
        }

        // Check if user has already voted for this review
        if (reviewHelpfulVoteRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            throw new DuplicateHelpfulVoteException("You have already marked this review as helpful");
        }

        // Create the helpful vote record
        ReviewHelpfulVote helpfulVote = ReviewHelpfulVote.builder()
                .review(review)
                .reviewId(reviewId)
                .user(user)
                .userId(userId)
                .build();

        reviewHelpfulVoteRepository.save(helpfulVote);

        // Increment the helpful count
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        Review savedReview = reviewRepository.save(review);

        log.info("Review helpful count updated to: {}", savedReview.getHelpfulCount());
        return ReviewResponse.fromEntity(savedReview, true); // User just voted, so wasHelpfulToCurrentUser = true
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

    private void uploadAndSaveReviewImages(Review review, MultipartFile[] images) {
        if (images == null || images.length == 0) {
            return;
        }

        // Validate image files and upload limit (max 5 images per review)
        if (images.length > 5) {
            throw new IllegalArgumentException("Maximum 5 images allowed per review");
        }

        List<ReviewImage> reviewImages = new ArrayList<>();
        
        for (int i = 0; i < images.length; i++) {
            MultipartFile imageFile = images[i];
            
            // Validate file
            if (imageFile.isEmpty()) {
                continue;
            }
            
            // Validate file type
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }
            
            // Validate file size (max 5MB)
            if (imageFile.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Image file size must be less than 5MB");
            }
            
            try {
                // Upload to CDN
                String imageUrl = cdnService.uploadImage(imageFile, "reviews");
                
                // Create ReviewImage entity
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(review)
                        .reviewId(review.getId())
                        .imageUrl(imageUrl)
                        .altText("Review image " + (i + 1))
                        .sortOrder(i)
                        .build();
                        
                reviewImages.add(reviewImage);
                log.debug("Uploaded review image: {}", imageUrl);
                
            } catch (IOException e) {
                log.error("Failed to upload review image for review {}", review.getId(), e);
                throw new RuntimeException("Failed to upload review image: " + e.getMessage(), e);
            }
        }
        
        // Save all review images
        if (!reviewImages.isEmpty()) {
            reviewImageRepository.saveAll(reviewImages);
            log.info("Saved {} images for review {}", reviewImages.size(), review.getId());
        }
    }
}