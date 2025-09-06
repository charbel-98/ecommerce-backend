package com.charbel.ecommerce.category.dto;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

	private String name;

	@Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
	private String slug;

	private String description;

	private UUID parentId;

	@Min(value = 0, message = "Sort order must be non-negative")
	private Integer sortOrder;

	private Boolean isActive;

	private MultipartFile image;
}
