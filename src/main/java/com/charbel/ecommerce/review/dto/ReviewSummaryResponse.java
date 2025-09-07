package com.charbel.ecommerce.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryResponse {

    private Long totalReviews;
    private BigDecimal averageRating;
    private Map<Integer, Long> ratingDistribution;

    public static ReviewSummaryResponse create(Long totalReviews, BigDecimal averageRating, Map<Integer, Long> ratingDistribution) {
        return ReviewSummaryResponse.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .build();
    }
}