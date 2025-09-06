package com.charbel.ecommerce.brand.dto;

import com.charbel.ecommerce.brand.entity.Brand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandResponse {

	private UUID id;
	private String name;
	private String slug;
	private String description;
	private String logoUrl;
	private String websiteUrl;
	private Brand.BrandStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static BrandResponse fromEntity(Brand brand) {
		return BrandResponse.builder().id(brand.getId()).name(brand.getName()).slug(brand.getSlug())
				.description(brand.getDescription()).logoUrl(brand.getLogoUrl()).websiteUrl(brand.getWebsiteUrl())
				.status(brand.getStatus()).createdAt(brand.getCreatedAt()).updatedAt(brand.getUpdatedAt()).build();
	}
}
