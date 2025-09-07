package com.charbel.ecommerce.review.dto;

import com.charbel.ecommerce.review.entity.ReviewImage;
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
public class ReviewImageResponse {

    private UUID id;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    public static ReviewImageResponse fromEntity(ReviewImage reviewImage) {
        return ReviewImageResponse.builder()
                .id(reviewImage.getId())
                .imageUrl(reviewImage.getImageUrl())
                .altText(reviewImage.getAltText())
                .sortOrder(reviewImage.getSortOrder())
                .createdAt(reviewImage.getCreatedAt())
                .build();
    }
}