package com.charbel.ecommerce.product.dto;

import com.charbel.ecommerce.product.entity.ProductVariant;
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

	public static ProductVariantResponse fromEntity(ProductVariant variant) {
		return ProductVariantResponse.builder().id(variant.getId()).sku(variant.getSku())
				.attributes(variant.getAttributes()).price(variant.getPrice()).stock(variant.getStock())
				.createdAt(variant.getCreatedAt()).updatedAt(variant.getUpdatedAt()).build();
	}
}
