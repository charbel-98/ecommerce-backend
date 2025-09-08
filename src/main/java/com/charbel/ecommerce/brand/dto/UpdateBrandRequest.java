package com.charbel.ecommerce.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateBrandRequest {

	@NotBlank(message = "Brand name is required")
	private String name;

	@NotBlank(message = "Brand slug is required")
	@Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
	private String slug;

	private String description;

	private String websiteUrl;

	private MultipartFile logo;
}