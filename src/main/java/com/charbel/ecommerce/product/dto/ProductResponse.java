package com.charbel.ecommerce.product.dto;

import com.charbel.ecommerce.product.entity.Product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
	
	private UUID id;
	private String name;
	private String description;
	private Integer basePrice;
	private ProductStatus status;
	private List<ProductVariantResponse> variants;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}