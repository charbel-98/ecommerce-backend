package com.charbel.ecommerce.review.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;

    @Size(max = 5, message = "Maximum 5 images allowed per review")
    private MultipartFile[] images;
}