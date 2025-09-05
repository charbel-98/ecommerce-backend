package com.charbel.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.UUID;

@Data
public class ProductImageRequest {
    
    private UUID variantId;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private String altText;
    
    private Boolean isPrimary = false;
    
    @PositiveOrZero(message = "Sort order must be non-negative")
    private Integer sortOrder = 0;
}