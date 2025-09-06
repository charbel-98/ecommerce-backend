package com.charbel.ecommerce.category.dto;

import com.charbel.ecommerce.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

	private UUID id;
	private String name;
	private String slug;
	private String description;
	private String imageUrl;
	private UUID parentId;
	private Integer level;
	private Integer sortOrder;
	private Boolean isActive;
	private List<CategoryResponse> children;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static CategoryResponse fromEntity(Category category) {
		return fromEntity(category, false);
	}

	public static CategoryResponse fromEntity(Category category, boolean includeChildren) {
		CategoryResponse response = CategoryResponse.builder().id(category.getId()).name(category.getName())
				.slug(category.getSlug()).description(category.getDescription()).imageUrl(category.getImageUrl())
				.parentId(category.getParentId()).level(category.getLevel()).sortOrder(category.getSortOrder())
				.isActive(category.getIsActive()).createdAt(category.getCreatedAt()).updatedAt(category.getUpdatedAt())
				.build();

		if (includeChildren && category.getChildren() != null) {
			response.setChildren(category.getChildren().stream().map(child -> CategoryResponse.fromEntity(child, true))
					.collect(Collectors.toList()));
		}

		return response;
	}
}
