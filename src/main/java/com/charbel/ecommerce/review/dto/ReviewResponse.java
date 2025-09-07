package com.charbel.ecommerce.review.dto;

import com.charbel.ecommerce.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse fromEntity(Review review) {
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
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}