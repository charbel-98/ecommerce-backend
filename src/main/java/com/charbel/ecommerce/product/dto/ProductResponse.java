package com.charbel.ecommerce.product.dto;

import com.charbel.ecommerce.brand.dto.BrandResponse;
import com.charbel.ecommerce.category.dto.CategoryResponse;
import com.charbel.ecommerce.common.enums.GenderType;
import com.charbel.ecommerce.product.entity.Product;
import com.charbel.ecommerce.product.entity.Product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
	
	private UUID id;
	private String name;
	private String description;
	private Integer basePrice;
	private BrandResponse brand;
	private CategoryResponse category;
	private GenderType gender;
	private ProductStatus status;
	private Map<String, Object> metadata;
	private List<ProductVariantResponse> variants;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ProductResponse fromEntity(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.basePrice(product.getBasePrice())
				.brand(product.getBrand() != null ? BrandResponse.fromEntity(product.getBrand()) : null)
				.category(product.getCategory() != null ? CategoryResponse.fromEntity(product.getCategory()) : null)
				.gender(product.getGender())
				.status(product.getStatus())
				.metadata(product.getMetadata())
				.variants(product.getVariants() != null 
					? product.getVariants().stream().map(ProductVariantResponse::fromEntity).collect(Collectors.toList())
					: null)
				.createdAt(product.getCreatedAt())
				.updatedAt(product.getUpdatedAt())
				.build();
	}
}