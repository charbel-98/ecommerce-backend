package com.charbel.ecommerce.category.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class CreateCategoryRequest {

	@NotBlank(message = "Category name is required")
	private String name;

	@NotBlank(message = "Category slug is required")
	@Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
	private String slug;

	private String description;

	@NotNull(message = "Parent ID is required")
	private UUID parentId;

	@NotNull(message = "Sort order is required")
	@Min(value = 0, message = "Sort order must be non-negative")
	private Integer sortOrder;

	private MultipartFile image;
}
