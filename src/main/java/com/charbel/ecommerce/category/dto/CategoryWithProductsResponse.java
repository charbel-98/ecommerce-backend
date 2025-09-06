package com.charbel.ecommerce.category.dto;

import com.charbel.ecommerce.product.dto.ProductResponse;
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
public class CategoryWithProductsResponse {

	private UUID id;
	private String name;
	private String slug;
	private String description;
	private UUID parentId;
	private Integer level;
	private Integer sortOrder;
	private Boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<ProductResponse> products;
}