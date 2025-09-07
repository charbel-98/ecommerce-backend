package com.charbel.ecommerce.review.dto;

import com.charbel.ecommerce.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private UUID id;
    private UUID productId;
    private UUID userId;
    private String userFirstName;
    private String userLastName;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private Boolean hasUserVoted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewImageResponse> images;

    public static ReviewResponse fromEntity(Review review) {
        List<ReviewImageResponse> imageResponses = null;
        if (review.getImages() != null) {
            imageResponses = review.getImages().stream()
                    .map(ReviewImageResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .userFirstName(review.getUser() != null ? review.getUser().getFirstName() : null)
                .userLastName(review.getUser() != null ? review.getUser().getLastName() : null)
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .hasUserVoted(false) // Default to false when current user context is unknown
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .images(imageResponses)
                .build();
    }

    public static ReviewResponse fromEntity(Review review, boolean hasUserVoted) {
        List<ReviewImageResponse> imageResponses = null;
        if (review.getImages() != null) {
            imageResponses = review.getImages().stream()
                    .map(ReviewImageResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .userFirstName(review.getUser() != null ? review.getUser().getFirstName() : null)
                .userLastName(review.getUser() != null ? review.getUser().getLastName() : null)
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .hasUserVoted(hasUserVoted)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .images(imageResponses)
                .build();
    }
}