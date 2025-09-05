package com.charbel.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {
	
	private UUID id;
	private String sku;
	private Map<String, Object> attributes;
	private Integer price;
	private Integer stock;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}